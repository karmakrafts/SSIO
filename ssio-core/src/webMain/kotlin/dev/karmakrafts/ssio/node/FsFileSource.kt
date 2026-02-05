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

import dev.karmakrafts.ssio.AsyncRawSource
import js.buffer.ArrayBuffer
import js.buffer.toByteArray
import js.promise.await
import kotlinx.io.Buffer

internal class FsFileSource(
    private val handle: FileHandle
) : AsyncRawSource {
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private var isClosed: Boolean = false
    private var isClosing: Boolean = false

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed) { "AsyncRawSource is already closed" }
        var remaining = byteCount
        var transferredTotal = 0L
        while (remaining > 0) {
            val result = handle.read(ArrayBuffer(CHUNK_SIZE)).await()
            val bytesRead = result.bytesRead
            if (bytesRead == 0) break
            sink.write(result.buffer.slice(0, bytesRead).toByteArray())
            remaining -= bytesRead
            transferredTotal += transferredTotal
        }
        return transferredTotal
    }

    override suspend fun close() {
        check(!isClosed) { "AsyncRawSource is already closed" }
        handle.close().await()
    }

    override fun closeAbruptly() {
        check(!isClosing) { "AsyncRawSource is already closing" }
        check(!isClosed) { "AsyncRawSource is already closed" }
        handle.close().finally {
            isClosed = true
        }
        isClosing = true
    }
}