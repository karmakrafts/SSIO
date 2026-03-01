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
import dev.karmakrafts.ssio.cio.CIOFileSink
import dev.karmakrafts.ssio.cio.CIOFileSource
import dev.karmakrafts.ssio.cio.NativeFile
import dev.karmakrafts.ssio.uring.URingFileSink
import dev.karmakrafts.ssio.uring.URingFileSource
import dev.karmakrafts.ssio.uring.isUringAvailable
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.PATH_MAX
import platform.posix.fsync
import platform.posix.getcwd
import platform.posix.getenv

private val fileSourceFactory: suspend (Path) -> AsyncRawSource = if (isUringAvailable) { path ->
    URingFileSource(NativeFile.create(path))
}
else { path -> CIOFileSource(NativeFile.create(path)) }

private val fileSinkFactory: suspend (Path, Boolean) -> AsyncRawSink = if (isUringAvailable) { path, append ->
    URingFileSink(NativeFile.create(path, true, append))
}
else { path, append -> CIOFileSink(NativeFile.create(path, true, append)) }

internal actual fun platformSyncFd(fd: Int) {
    fsync(fd)
}

internal actual fun platformGetCwd(): String = memScoped {
    val buffer = allocArray<ByteVar>(PATH_MAX)
    getcwd(buffer, PATH_MAX.toULong())
    buffer.toKStringFromUtf8()
}

internal actual fun platformGetTmpDir(): String {
    val dirAddress = getenv("TMPDIR")
    if (dirAddress != null) return dirAddress.toKStringFromUtf8()
    return "/tmp"
}

internal actual suspend fun createFileSource(path: Path): AsyncRawSource = fileSourceFactory(path)
internal actual suspend fun createFileSink(path: Path, append: Boolean): AsyncRawSink = fileSinkFactory(path, append)