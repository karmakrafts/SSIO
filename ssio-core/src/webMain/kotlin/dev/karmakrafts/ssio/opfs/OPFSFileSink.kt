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

import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.asInt8Array
import js.typedarrays.Int8Array
import kotlinx.io.Buffer
import web.fs.FileSystemWritableFileStream
import web.fs.write
import web.streams.close
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.math.min

internal class OPFSFileSink( // @formatter:off
    private val stream: FileSystemWritableFileStream
) : AsyncRawSink { // @formatter:on
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private var isClosing: Boolean = false
    private var isClosed: Boolean = false
    private var buffer: ByteArray = ByteArray(CHUNK_SIZE)

    override suspend fun write(source: Buffer, byteCount: Long) {
        check(!isClosed) { "OPFSFileSink is already closed" }
        var remaining = min(source.size, byteCount)
        while (remaining > 0) {
            val toWrite = min(CHUNK_SIZE.toLong(), remaining).toInt()
            val bytesRead = source.readAtMostTo(buffer, 0, toWrite)
            if (bytesRead == -1) break
            stream.write(Int8Array(buffer.asInt8Array().buffer, 0, bytesRead))
            remaining -= toWrite
        }
    }

    override suspend fun flush() {
        check(!isClosed) { "OpfsFileSink is already closed" }
        // Flush is a NOOP for OPFS sinks
    }

    override suspend fun close() {
        check(!isClosed) { "OpfsFileSink is already closed" }
        stream.close()
        isClosed = true
    }

    override fun closeAbruptly() {
        check(!isClosed) { "OpfsFileSink is already closed" }
        check(!isClosing) { "OpfsFileSink is already closing" }
        stream.closeAsync().finally {
            isClosed = true
        }
        isClosing = true
    }
}