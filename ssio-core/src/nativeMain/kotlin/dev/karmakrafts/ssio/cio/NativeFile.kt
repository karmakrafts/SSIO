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

import dev.karmakrafts.ssio.AsyncSystemFileSystem
import dev.karmakrafts.ssio.api.AsyncCloseable
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.platformSyncFd
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.posix.O_APPEND
import platform.posix.O_CREAT
import platform.posix.O_RDONLY
import platform.posix.O_RDWR
import kotlin.concurrent.atomics.AtomicBoolean
import platform.posix.close as posixClose
import platform.posix.open as posixOpen
import platform.posix.read as posixRead
import platform.posix.write as posixWrite

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class NativeFile( // @formatter:off
    val fd: Int
) : AsyncCloseable { // @formatter:on
    companion object {
        suspend fun create(
            path: Path, writable: Boolean = false, append: Boolean = false, auxFlags: Int = 0
        ): NativeFile {
            path.parent?.let { parent -> AsyncSystemFileSystem.createDirectories(parent) }
            return NativeFile(path, writable, append, auxFlags)
        }
    }

    private val isClosed: AtomicBoolean = AtomicBoolean(false)

    // @formatter:off
    constructor(path: Path, openFlags: Int, accessFlags: Int = 0x1A4) :
        this(Unit.run {
            posixOpen(path.toString(), openFlags, accessFlags)
        })

    constructor(path: Path, writable: Boolean = false, append: Boolean = false, auxFlags: Int = 0) :
        this(path, Unit.run {
            var openFlags = O_CREAT
            openFlags = openFlags or if(writable) O_RDWR else O_RDONLY
            if (append) openFlags = openFlags or O_APPEND
            openFlags or auxFlags
        })
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