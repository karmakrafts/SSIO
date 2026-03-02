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

import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.AsynchronousFileChannel
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicLong
import java.nio.file.Path as NioPath

internal class NIOFileSource(
    path: NioPath
) : AsyncRawSource {
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val channel: AsynchronousFileChannel = AsynchronousFileChannel.open(path)
    private val fileSize: Long = channel.size()
    private val offset: AtomicLong = AtomicLong(0L)
    private val buffer: ByteArray = ByteArray(CHUNK_SIZE)
    private val nioBuffer: ByteBuffer = ByteBuffer.allocateDirect(CHUNK_SIZE).order(ByteOrder.nativeOrder())

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        if (offset.load() == fileSize) return -1L // We already reached EOF
        var readTotal = 0L
        while (readTotal < byteCount) {
            nioBuffer.clear()
            val bytesRead = channel.read(nioBuffer, offset.load()).await()
            nioBuffer.flip()
            if (bytesRead == -1) break // We reached EOF while reading the current chunk
            nioBuffer.get(buffer, 0, bytesRead)
            sink.write(buffer, 0, bytesRead)
            readTotal += bytesRead
            offset.fetchAndAdd(bytesRead.toLong())
        }
        return readTotal
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