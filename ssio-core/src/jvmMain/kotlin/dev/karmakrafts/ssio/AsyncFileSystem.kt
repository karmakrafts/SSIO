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
import dev.karmakrafts.ssio.nio.NioFileSink
import dev.karmakrafts.ssio.nio.NioFileSource
import kotlin.io.path.absolute
import java.nio.file.Paths as NioPaths

private object AsyncFileSystemImpl : AbstractAsyncFileSystem() {
    override suspend fun getWorkingDirectory(): Path = NioPaths.get("").absolute().toSsioPath()
    override suspend fun getTempDirectory(): Path = Path(System.getProperty("java.io.tmpdir"))
    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink =
        NioFileSink(path.toNioPath().normalize().absolute())

    override suspend fun source(path: Path): AsyncRawSource = NioFileSource(path.toNioPath().normalize().absolute())
}

actual val AsyncSystemFileSystem: AsyncFileSystem = AsyncFileSystemImpl