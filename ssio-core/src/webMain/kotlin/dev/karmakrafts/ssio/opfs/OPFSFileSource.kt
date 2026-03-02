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

@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.karmakrafts.ssio.opfs

import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.asByteArray
import js.typedarrays.toInt8Array
import kotlinx.io.Buffer
import web.blob.bytes
import web.file.File
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.math.min

internal class OPFSFileSource( // @formatter:off
    private val file: File
) : AsyncRawSource { // @formatter:on
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private var isClosed: Boolean = false
    private var offset: Long = 0L

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed) { "OpfsFileSource is already closed" }
        var remaining = byteCount
        var readTotal = 0L
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining)
            val subSlice = file.slice(offset.toDouble(), (offset + chunkSize).toDouble())
            val data = subSlice.bytes()
            val bytesRead = data.length
            if (bytesRead == 0) return -1L // We reached EOF
            sink.write(data.toInt8Array().asByteArray(), 0, bytesRead)
            remaining -= chunkSize
            readTotal += bytesRead
        }
        offset += readTotal
        return readTotal
    }

    override suspend fun close() {
        check(!isClosed) { "OpfsFileSource is already closed" }
        isClosed = true
    }

    override fun closeAbruptly() {
        check(!isClosed) { "OpfsFileSource is already closed" }
        isClosed = true
    }
}