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

import dev.karmakrafts.ssio.AsyncRawSink
import dev.karmakrafts.ssio.files.Path
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.yield
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import platform.posix.O_APPEND
import platform.posix.O_CREAT
import platform.posix.O_RDWR
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min
import platform.posix.close as posixClose
import platform.posix.open as posixOpen
import platform.posix.write as posixWrite

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class NativeFileSink(path: Path, append: Boolean) : AsyncRawSink {
    companion object {
        private const val DEFAULT_MASK: Int = 0x1A4 // 0644
        private const val CHUNK_SIZE: Int = 4096
    }

    private val openFlags: Int = run {
        val baseFlags = O_CREAT or O_RDWR
        if (append) baseFlags or O_APPEND
        else baseFlags
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val fd: Int = posixOpen(path.toString(), openFlags, DEFAULT_MASK)

    override suspend fun write(source: Buffer, byteCount: Long) {
        check(!isClosed.load()) { "AsyncRawSink is already closed" }
        val toWrite = min(source.size, byteCount)
        var remaining = toWrite
        while (remaining > 0) {
            val chunkSize = min(CHUNK_SIZE.toLong(), remaining).toInt()
            source.readByteArray(chunkSize).usePinned { pinnedChunk ->
                posixWrite(fd, pinnedChunk.addressOf(0), chunkSize.convert())
            }
            remaining -= chunkSize
            yield() // Make sure we yield manually after every chunk to keep things non-blocking
        }
    }

    override suspend fun flush() {
        check(!isClosed.load()) { "AsyncRawSink is already closed" }
        platformSyncFd(fd)
    }

    override suspend fun close() {
        closeAbruptly()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncRawSink is already closed" }
        posixClose(fd)
    }
}