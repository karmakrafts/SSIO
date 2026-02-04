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

import dev.karmakrafts.ssio.AsyncRawSource
import js.buffer.ArrayBuffer
import js.promise.await
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import kotlinx.io.Buffer
import web.streams.ReadableStreamDefaultReader
import web.streams.cancel
import web.streams.read
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.math.min

internal class OpfsFileSource( // @formatter:off
    private val reader: ReadableStreamDefaultReader<Uint8Array<ArrayBuffer>>,
) : AsyncRawSource { // @formatter:on
    private val buffer: Buffer = Buffer()
    private var isClosing: Boolean = false
    private var isClosed: Boolean = false

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed) { "OpfsFileSource is already closed" }
        var transferredTotal = 0L
        // First check buffer for leftovers
        if (buffer.size > 0) {
            val toRead = min(buffer.size, byteCount)
            transferredTotal += buffer.readAtMostTo(sink, toRead)
            // If we got enough data from the buffer, we can return early
            if (transferredTotal == byteCount) return transferredTotal
        }
        // Otherwise we have to pull in fresh data..
        val missingTotal = byteCount - transferredTotal
        var read = 0L
        while (true) {
            val result = reader.read()
            if (result.done) break // We reached EOF
            val data = result.value?.toByteArray() ?: ByteArray(0)
            val dataSize = data.size.toLong()
            read += dataSize
            when {
                read > missingTotal -> {
                    // First write the remaining data to fulfill consumer
                    val toRead = missingTotal - read
                    sink.write(data.sliceArray(0..<toRead.toInt()))
                    // If we overfetched some data, we need to write it back to the buffer
                    val toWrite = dataSize - toRead
                    buffer.write(data.sliceArray(toRead.toInt()..<dataSize.toInt()))
                }

                else -> sink.write(data) // Otherwise just write the data
            }
        }
        // Clamp read byte count between 0 and byteCount
        return min(read, byteCount)
    }

    override suspend fun close() {
        check(!isClosed) { "OpfsFileSource is already closed" }
        reader.cancel()
        reader.closed.await()
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