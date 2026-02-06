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

package dev.karmakrafts.ssio.posix

import dev.karmakrafts.ssio.AsyncRawSource
import dev.karmakrafts.ssio.files.Path
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.yield
import kotlinx.io.Buffer
import platform.posix.O_RDONLY
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min
import platform.posix.close as posixClose
import platform.posix.open as posixOpen
import platform.posix.read as posixRead

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class NativeFileSource(
    path: Path
) : AsyncRawSource {
    companion object {
        private const val DEFAULT_MASK: Int = 0x1A4 // 0644
        private const val CHUNK_SIZE: Int = 4096
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val fd: Int = posixOpen(path.toString(), O_RDONLY, DEFAULT_MASK)

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed.load()) { "AsyncRawSource is already closed" }
        var remaining = byteCount
        var readTotal = 0L
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining).toInt()
            val chunk = ByteArray(chunkSize)
            val bytesRead = chunk.usePinned { pinnedChunk ->
                posixRead(fd, pinnedChunk.addressOf(0), chunkSize.convert())
            }
            if (bytesRead <= 0) break
            sink.write(chunk)
            remaining -= bytesRead
            readTotal += bytesRead
            yield() // Make sure we yield manually after every chunk to keep things non-blocking
        }
        return readTotal
    }

    override suspend fun close() {
        closeAbruptly()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncRawSource is already closed" }
        posixClose(fd)
    }
}