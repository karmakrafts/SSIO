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

import dev.karmakrafts.ssio.AsyncRawSource
import js.buffer.ArrayBuffer
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import kotlinx.io.Buffer
import web.streams.ReadableStreamDefaultReader
import web.streams.cancel
import web.streams.read
import kotlin.math.min

internal class OPFSFileSource( // @formatter:off
    private val reader: ReadableStreamDefaultReader<Uint8Array<ArrayBuffer>>,
    private val size: Long
) : AsyncRawSource { // @formatter:on
    private var isClosed: Boolean = false

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed) { "OPFSFileSource is already closed" }
        var remaining = min(this.size, byteCount)
        if (remaining == 0L) return -1L
        var readTotal = 0L
        while (remaining > 0) {
            val result = reader.read()
            if (result.done) break
            val data = result.value?.toByteArray() ?: break
            sink.write(data)
            val dataSize = data.size.toLong()
            remaining -= dataSize
            readTotal += dataSize
        }
        return readTotal
    }

    override suspend fun close() {
        check(!isClosed) { "OPFSFileSource is already closed" }
        reader.cancel()
        isClosed = true
    }

    override fun closeAbruptly() {
        check(!isClosed) { "OPFSFileSource is already closed" }
        reader.cancelAsync().finally {
            isClosed = true
        }
    }
}