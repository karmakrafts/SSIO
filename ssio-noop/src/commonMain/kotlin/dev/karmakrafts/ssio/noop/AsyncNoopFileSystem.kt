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

package dev.karmakrafts.ssio.noop

import dev.karmakrafts.ssio.api.AsyncFileSystem
import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.Paths
import kotlinx.io.files.FileMetadata

object AsyncNoopFileSystem : AsyncFileSystem {
    override suspend fun getWorkingDirectory(): Path = Paths.root
    override suspend fun getTempDirectory(): Path = Paths.root
    override suspend fun move(oldPath: Path, newPath: Path) = Unit
    override suspend fun delete(path: Path, mustExist: Boolean) = Unit
    override suspend fun createDirectories(path: Path, mustCreate: Boolean) = Unit
    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink = NoopFileSink
    override suspend fun source(path: Path): AsyncRawSource = NoopFileSource
    override suspend fun exists(path: Path): Boolean = false
    override suspend fun resolve(path: Path): Path = path
    override suspend fun metadataOrNull(path: Path): FileMetadata? = null
    override suspend fun list(path: Path): List<Path> = emptyList()
}