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

package dev.karmakrafts.ssio.cio

import dev.karmakrafts.ssio.api.AsyncRawSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import kotlinx.io.Buffer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class CIOFileSource(
    private val file: NativeFile
) : AsyncRawSource {
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val buffer: ByteArray = ByteArray(CHUNK_SIZE)
    private val pinnedBuffer: Pinned<ByteArray> = buffer.pin()

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed.load()) { "AsyncRawSource is already closed" }
        var remaining = byteCount
        var readTotal = 0L
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining).toInt()
            val bytesRead = file.read(pinnedBuffer.addressOf(0), chunkSize.toUInt())
            if (bytesRead <= 0) break
            sink.write(buffer, 0, bytesRead.toInt())
            remaining -= bytesRead
            readTotal += bytesRead
        }
        return readTotal
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncRawSource is already closed" }
        pinnedBuffer.unpin()
        file.close()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncRawSource is already closed" }
        pinnedBuffer.unpin()
        file.closeAbruptly()
    }
}