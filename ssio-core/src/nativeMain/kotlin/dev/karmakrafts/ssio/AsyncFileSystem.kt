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

package dev.karmakrafts.ssio

import dev.karmakrafts.ssio.files.Path
import dev.karmakrafts.ssio.posix.NativeFileSink
import dev.karmakrafts.ssio.posix.NativeFileSource
import dev.karmakrafts.ssio.posix.platformGetCwd
import dev.karmakrafts.ssio.posix.platformGetTmpDir

private object AsyncFileSystemImpl : AbstractAsyncFileSystem() {
    private val workingDirectory: Path = Path(platformGetCwd())
    private val tempDirectory: Path = Path(platformGetTmpDir())

    override suspend fun getWorkingDirectory(): Path = workingDirectory
    override suspend fun getTempDirectory(): Path = tempDirectory
    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink = NativeFileSink(path, append)
    override suspend fun source(path: Path): AsyncRawSource = NativeFileSource(path)
}

actual val AsyncSystemFileSystem: AsyncFileSystem = AsyncFileSystemImpl