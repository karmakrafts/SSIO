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

package dev.karmakrafts.ssio.uring

import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.cio.NativeFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import kotlinx.io.Buffer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class)
internal class URingFileSink( // @formatter:off
    private val file: NativeFile,
) : AsyncRawSink { // @formatter:on
    companion object {
        private const val CHUNK_SIZE: Int = 512 // As suggested by benchmarks
    }

    private val buffer: ByteArray = ByteArray(CHUNK_SIZE)
    private val isClosed: AtomicBoolean = AtomicBoolean(false)

    override suspend fun write(source: Buffer, byteCount: Long) {
        check(!isClosed.load()) { "URingFileSink is already closed" }
        var remaining = min(source.size, byteCount)
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining).toInt()
            val bytesRead = source.readAtMostTo(buffer, 0, chunkSize)
            if (bytesRead == -1) break // We reached EOF
            val pinnedBuffer = buffer.pin()
            uringDispatcher.enqueue { entry ->
                entry.prepareWrite(file.fd, pinnedBuffer.addressOf(0), bytesRead.toUInt())
            }.join()
            pinnedBuffer.unpin()
            remaining -= chunkSize
        }
    }

    override suspend fun flush() {
        check(!isClosed.load()) { "URingFileSink is already closed" }
        uringDispatcher.enqueue { entry ->
            entry.prepareFlush(file.fd)
        }.join()
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "URingFileSink is already closed"
        }
        file.close()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "URingFileSink is already closed"
        }
        file.closeAbruptly()
    }
}