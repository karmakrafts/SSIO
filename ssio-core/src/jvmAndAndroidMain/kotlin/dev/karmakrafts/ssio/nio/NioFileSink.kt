/*
 * Copyright 2026 Karma Krafts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.ssio.nio

import dev.karmakrafts.ssio.AsyncRawSink
import dev.karmakrafts.ssio.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.StandardOpenOption
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.math.min
import java.nio.file.Path as NioPath

internal class NioFileSink(
    path: NioPath
) : AsyncRawSink {
    companion object {
        private const val CHUNK_SIZE: Long = 4096
    }

    init {
        if (!path.exists()) {
            path.createParentDirectories()
            path.createFile()
        }
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val channel: AsynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE)

    override suspend fun write(source: Buffer, byteCount: Long) {
        val toWrite = min(source.size, byteCount)
        var remaining = toWrite
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE, remaining).toInt()
            val chunk = source.readByteArray(chunkSize)
            channel.write(ByteBuffer.wrap(chunk), 0).await()
            remaining -= chunkSize
        }
    }

    override suspend fun flush() {
        check(!isClosed.load()) { "AsyncRawSink is already closed" }
        withContext(Dispatchers.IO) {
            channel.force(true)
        }
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncRawSink is already closed" }
        withContext(Dispatchers.IO) {
            channel.close()
        }
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncRawSink is already closed" }
        channel.close()
    }
}