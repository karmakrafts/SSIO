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

import dev.karmakrafts.ssio.api.AsyncCloseable
import dev.karmakrafts.ssio.api.Path
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.posix.O_RDONLY
import kotlin.concurrent.atomics.AtomicBoolean
import platform.posix.close as posixClose
import platform.posix.open as posixOpen
import platform.posix.read as posixRead
import platform.posix.write as posixWrite

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class NativeFile( // @formatter:off
    val fd: Int
) : AsyncCloseable { // @formatter:on
    private val isClosed: AtomicBoolean = AtomicBoolean(false)

    // @formatter:off
    constructor(path: Path, openFlags: Int = O_RDONLY, accessFlags: Int = 0x1A4) :
        this(posixOpen(path.toString(), openFlags, accessFlags))
    // @formatter:on

    override fun equals(other: Any?): Boolean {
        return if (other !is NativeFile) false
        else fd == other.fd
    }

    override fun hashCode(): Int = fd.hashCode()
    override fun toString(): String = "NativeFile[fd=$fd]"

    suspend fun flush() = withContext(Dispatchers.IO) {
        platformSyncFd(fd)
    }

    suspend fun read(buffer: COpaquePointer, bufferSize: UInt): Long = withContext(Dispatchers.IO) {
        posixRead(fd, buffer, bufferSize.convert()).convert()
    }

    suspend fun write(buffer: COpaquePointer, bufferSize: UInt) = withContext(Dispatchers.IO) {
        posixWrite(fd, buffer, bufferSize.convert())
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "NativeFile is already closed"
        }
        withContext(Dispatchers.IO) {
            posixClose(fd)
        }
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "NativeFile is already closed"
        }
        posixClose(fd)
    }
}