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

package dev.karmakrafts.ssio.node

import dev.karmakrafts.ssio.AsyncRawSink
import dev.karmakrafts.ssio.asInt8Array
import js.promise.await
import kotlinx.io.Buffer
import kotlin.math.min

internal class NodeFileSink(
    private val handle: FileHandle
) : AsyncRawSink {
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private var isClosing: Boolean = false
    private var isClosed: Boolean = false
    private val buffer: ByteArray = ByteArray(CHUNK_SIZE)

    override suspend fun write(source: Buffer, byteCount: Long) {
        val toWrite = min(source.size, byteCount)
        var remaining = toWrite
        while (remaining > 0) {
            if (source.exhausted()) break // Stop when source is exhausted prematurely
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining).toInt()
            val bytesRead = source.readAtMostTo(buffer, 0, chunkSize)
            if (bytesRead == -1) break
            handle.write(buffer.asInt8Array(), 0, bytesRead).await()
            remaining -= bytesRead
        }
    }

    override suspend fun flush() {
        handle.sync().await()
    }

    override suspend fun close() {
        check(!isClosed) { "NodeFileSink is already closed" }
        handle.close().await()
        isClosed = true
    }

    override fun closeAbruptly() {
        check(!isClosed) { "NodeFileSink is already closed" }
        check(!isClosing) { "NodeFileSink is already closing" }
        handle.close().finally {
            isClosed = true
            isClosing = false
        }
        isClosing = true
    }
}