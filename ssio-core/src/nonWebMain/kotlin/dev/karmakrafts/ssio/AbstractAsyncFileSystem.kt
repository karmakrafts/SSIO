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
import dev.karmakrafts.ssio.api.InternalSsioApi
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.div
import dev.karmakrafts.ssio.api.normalize
import dev.karmakrafts.ssio.api.platform
import dev.karmakrafts.ssio.api.toKxio
import dev.karmakrafts.ssio.api.toSsio
import kotlinx.io.files.Path as KxioPath
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.SystemFileSystem

abstract class AbstractAsyncFileSystem : AsyncFileSystem {
    override suspend fun resolve(path: Path): Path {
        return if (path.isAbsolute) path.normalize()
        else getWorkingDirectory() / path.normalize()
    }

    override suspend fun exists(path: Path): Boolean = SystemFileSystem.exists(path.toKxio())
    override suspend fun move(oldPath: Path, newPath: Path) =
        SystemFileSystem.atomicMove(oldPath.toKxio(), newPath.toKxio())

    override suspend fun delete(path: Path, mustExist: Boolean) = SystemFileSystem.delete(path.toKxio(), mustExist)
    override suspend fun metadataOrNull(path: Path): FileMetadata? = SystemFileSystem.metadataOrNull(path.toKxio())

    @OptIn(InternalSsioApi::class)
    override suspend fun list(path: Path): List<Path> {
        return if (!exists(path)) emptyList()
        else when {
            // Since kotlinx.io on Windows allows mixing separators, we sanitize everything
            // TODO: find a better solution for this or implement list from scratch?
            platform.isWindows -> SystemFileSystem.list(path.toKxio())
                .map { entry -> Path(entry.toString().replace('/', '\\')) }

            else -> SystemFileSystem.list(path.toKxio()).map(KxioPath::toSsio)
        }
    }

    override suspend fun createDirectories(path: Path, mustCreate: Boolean) {
        SystemFileSystem.createDirectories(path.toKxio(), mustCreate)
    }
}