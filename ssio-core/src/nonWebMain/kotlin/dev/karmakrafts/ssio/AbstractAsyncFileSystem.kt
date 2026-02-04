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

import dev.karmakrafts.ssio.files.div
import dev.karmakrafts.ssio.files.normalize
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

abstract class AbstractAsyncFileSystem : AsyncFileSystem {
    override suspend fun resolve(path: Path): Path {
        return if (path.isAbsolute) path.normalize()
        else getWorkingDirectory() / path.normalize()
    }

    override suspend fun exists(path: Path): Boolean = SystemFileSystem.exists(path)
    override suspend fun move(oldPath: Path, newPath: Path) = SystemFileSystem.atomicMove(oldPath, newPath)
    override suspend fun delete(path: Path, mustExist: Boolean) = SystemFileSystem.delete(path, mustExist)
    override suspend fun metadataOrNull(path: Path): FileMetadata? = SystemFileSystem.metadataOrNull(path)

    override suspend fun list(path: Path): List<Path> {
        return if (!exists(path)) emptyList()
        else SystemFileSystem.list(resolve(path)).toList()
    }

    override suspend fun createDirectories(path: Path, mustCreate: Boolean) {
        SystemFileSystem.createDirectories(path, mustCreate)
    }
}