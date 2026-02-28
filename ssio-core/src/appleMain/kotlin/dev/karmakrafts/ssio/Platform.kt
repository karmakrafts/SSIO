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

@file:OptIn(ExperimentalForeignApi::class)

package dev.karmakrafts.ssio

import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.cio.NativeFile
import dev.karmakrafts.ssio.cio.CIOFileSink
import dev.karmakrafts.ssio.cio.CIOFileSource
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.O_APPEND
import platform.posix.O_CREAT
import platform.posix.O_RDWR
import platform.posix.fsync
import platform.posix.getcwd
import platform.posix.getenv

internal actual fun platformSyncFd(fd: Int) {
    fsync(fd)
}

@OptIn(UnsafeNumber::class)
internal actual fun platformGetCwd(): String = memScoped {
    val buffer = allocArray<ByteVar>(4096)
    getcwd(buffer, 4096U)
    buffer.toKStringFromUtf8()
}

internal actual fun platformGetTmpDir(): String {
    val dirAddress = getenv("TMPDIR")
    if (dirAddress != null) return dirAddress.toKStringFromUtf8()
    return "/tmp"
}

internal actual suspend fun createFileSource(path: Path): AsyncRawSource = CIOFileSource(NativeFile(path))

internal actual suspend fun createFileSink(path: Path, append: Boolean): AsyncRawSink {
    var openFlags = O_CREAT or O_RDWR
    if (append) openFlags = openFlags or O_APPEND
    path.parent?.let { parent -> AsyncSystemFileSystem.createDirectories(parent) }
    return CIOFileSink(NativeFile(path, openFlags))
}