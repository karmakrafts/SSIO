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

package dev.karmakrafts.ssio.opfs

import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.asByteArray
import js.typedarrays.Int8Array
import kotlinx.io.Buffer
import web.streams.ReadableStreamBYOBReader
import web.streams.cancel
import web.streams.read
import kotlin.math.min

/**
 * Same as [OPFSFileSource], except that it uses [web.streams.ReadableStreamBYOBReader] when available
 * to reduce buffer overhead.
 */
internal class FastOPFSFileSource(
    private val reader: ReadableStreamBYOBReader
) : AsyncRawSource {
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private var isClosed: Boolean = false
    private var isClosing: Boolean = false

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        var remaining = byteCount
        var readTotal = 0L
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining).toInt()
            val result = reader.read(Int8Array(CHUNK_SIZE))
            if (result.done) return -1L
            val resultView = result.value ?: return -1L
            val bytesRead = resultView.byteLength
            sink.write(resultView.asByteArray(), 0, bytesRead)
            remaining -= chunkSize
            readTotal += bytesRead
        }
        return readTotal
    }

    override suspend fun close() {
        check(!isClosed) { "OpfsFileSink is already closed" }
        reader.cancel()
        isClosed = true
    }

    override fun closeAbruptly() {
        check(!isClosed) { "OpfsFileSource is already closed" }
        check(!isClosing) { "OpfsFileSource is already closing" }
        reader.cancelAsync().finally {
            isClosed = true
        }
        isClosing = true
    }
}