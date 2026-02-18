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

import dev.karmakrafts.ssio.api.AsyncFileSystem
import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.nio.NioFileSink
import dev.karmakrafts.ssio.nio.NioFileSource
import kotlin.io.path.absolute

private object AsyncFileSystemImpl : AbstractAsyncFileSystem() {
    override suspend fun getWorkingDirectory(): Path = AndroidFileSystem.context.filesDir.toSsioPath()
    override suspend fun getTempDirectory(): Path = AndroidFileSystem.context.dataDir.toSsioPath()
    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink =
        NioFileSink(path.toNioPath().normalize().absolute())

    override suspend fun source(path: Path): AsyncRawSource = NioFileSource(path.toNioPath().normalize().absolute())
}

actual val AsyncSystemFileSystem: AsyncFileSystem = AsyncFileSystemImpl