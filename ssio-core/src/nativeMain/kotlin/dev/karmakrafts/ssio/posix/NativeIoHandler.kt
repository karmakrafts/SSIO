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

import dev.karmakrafts.ssio.AsyncSystemFileSystem
import dev.karmakrafts.ssio.files.Path
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import platform.posix.O_RDONLY
import platform.posix.atexit
import platform.posix.open as posixOpen

@OptIn(ExperimentalForeignApi::class)
internal object NativeIoHandler {
    private val coroutineScope: CoroutineScope = CoroutineScope(
        Dispatchers.IO + CoroutineName("SSIO NativeIoHandler") + SupervisorJob()
    ) // TODO: Add CoroutineExceptionHandler

    init {
        atexit(staticCFunction<Unit> {
            val self = NativeIoHandler
            self.shutdown()
        })
    }

    suspend fun openFile( // @formatter:off
        path: Path,
        openFlags: Int = O_RDONLY,
        accessFlags: Int = 0x1A4
    ): NativeFile { // @formatter:on
        val fd = posixOpen(AsyncSystemFileSystem.resolve(path).toString(), openFlags, accessFlags)
        check(fd != -1) { "Could not open file $path" }
        return NativeFile(fd, coroutineScope)
    }

    private fun shutdown() {
        runBlocking {
            val supervisor = coroutineScope.coroutineContext.job
            supervisor.cancelAndJoin()
        }
    }
}