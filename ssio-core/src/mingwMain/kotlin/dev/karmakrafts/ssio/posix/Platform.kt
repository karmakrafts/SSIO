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

package dev.karmakrafts.ssio.posix

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix._get_osfhandle
import platform.posix._getcwd
import platform.posix.fflush
import platform.posix.wchar_tVar
import platform.windows.GetTempPathW
import platform.windows.MAX_PATH

internal actual fun platformSyncFd(fd: Int) {
    fflush(_get_osfhandle(fd).toCPointer())
}

internal actual fun platformGetCwd(): String = memScoped {
    val buffer = allocArray<ByteVar>(MAX_PATH)
    _getcwd(buffer, MAX_PATH)
    buffer.toKStringFromUtf8()
}

internal actual fun platformGetTmpDir(): String = memScoped {
    val buffer = allocArray<wchar_tVar>(MAX_PATH)
    GetTempPathW(MAX_PATH.convert(), buffer)
    buffer.toKStringFromUtf16()
}