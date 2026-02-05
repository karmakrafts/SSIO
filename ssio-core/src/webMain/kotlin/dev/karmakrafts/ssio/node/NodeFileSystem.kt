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

package dev.karmakrafts.ssio.node

import dev.karmakrafts.ssio.AbstractAsyncFileSystem
import dev.karmakrafts.ssio.AsyncRawSink
import dev.karmakrafts.ssio.AsyncRawSource
import dev.karmakrafts.ssio.files.Path
import kotlinx.io.files.FileMetadata

internal object NodeFileSystem : AbstractAsyncFileSystem() {
    override suspend fun getWorkingDirectory(): Path = Path(process.cwd())
    override suspend fun getTempDirectory(): Path = Path(Os.tmpdir())

    override suspend fun list(path: Path): List<Path> {
        val entries = FsPromises.readdir(path.toString())
        return entries.map(::Path)
    }

    override suspend fun createDirectories(path: Path, mustCreate: Boolean) {
        if (exists(path)) {
            if (mustCreate) error("Cannot create directory at $path as it already exists")
            else return
        }
        FsPromises.mkdir(path.toString())
    }

    override suspend fun metadataOrNull(path: Path): FileMetadata? {
        if (!exists(path)) return null
        val stats = FsPromises.stat(path.toString())
        return when {
            stats.isFile -> FileMetadata( // @formatter:off
                isRegularFile = true,
                isDirectory = false,
                size = stats.size
            ) // @formatter:on
            stats.isDirectory -> FileMetadata( // @formatter:off
                isRegularFile = false,
                isDirectory = true,
                size = 0L
            ) // @formatter:on
            else -> null
        }
    }

    override suspend fun source(path: Path): AsyncRawSource {
        val handle = FsPromises.open(path.toString(), "r")
        return NodeFileSource(handle)
    }

    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink {
        val mode = if (append) "a+" else "w+"
        path.parent?.let { parent -> createDirectories(parent) }
        val handle = FsPromises.open(path.toString(), mode)
        return NodeFileSink(handle)
    }

    override suspend fun exists(path: Path): Boolean = FsPromises.access(path.toString())

    override suspend fun move(oldPath: Path, newPath: Path) {
        if (oldPath == newPath) return
        FsPromises.rename(oldPath.toString(), newPath.toString())
    }

    override suspend fun delete(path: Path, mustExist: Boolean) {
        if (!exists(path)) {
            if (mustExist) error("Cannot delete file $path as it does not exist")
            else return
        }
        FsPromises.rm(path.toString())
    }
}