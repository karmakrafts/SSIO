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

package dev.karmakrafts.ssio.vfs

import dev.karmakrafts.ssio.api.AsyncCloseable
import dev.karmakrafts.ssio.api.AsyncFileSystem
import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.Paths
import dev.karmakrafts.ssio.api.div
import dev.karmakrafts.ssio.api.getSegments
import dev.karmakrafts.ssio.api.normalize
import kotlinx.io.Buffer
import kotlinx.io.files.FileMetadata

class AsyncVirtualFileSystem( // @formatter:off
    private val workingDirectory: Path = Paths.root,
    private val tempDirectory: Path = Paths.root / "tmp"
) : AsyncFileSystem, AsyncCloseable { // @formatter:on
    private val rootNode: VfsDirectoryNode = VfsDirectoryNode(Paths.separator)

    override suspend fun getWorkingDirectory(): Path = workingDirectory

    override suspend fun getTempDirectory(): Path = tempDirectory

    private suspend fun getDirectoryNode(path: Path, create: Boolean = false): Result<VfsDirectoryNode> {
        val resolvedPath = resolve(path)
        return if (resolvedPath.toString().isEmpty() || (resolvedPath.isAbsolute && resolvedPath.getSegments()
                .isEmpty())
        ) Result.success(rootNode)
        else {
            var node = rootNode
            for (segment in path.getSegments()) {
                try {
                    node = if (create) node.getOrCreateDirectory(segment)
                    else node[segment] as VfsDirectoryNode
                } catch (error: Throwable) {
                    return Result.failure(error)
                }
            }
            Result.success(node)
        }
    }

    private suspend fun getFileNode(path: Path, create: Boolean = true): Result<VfsFileNode> {
        val resolvedPath = resolve(path)
        val parentPath = resolvedPath.parent ?: return try {
            Result.success(
                if (create) rootNode.getOrCreateFile(path.name)
                else rootNode[path.name] as VfsFileNode
            )
        } catch (error: Throwable) {
            Result.failure(error)
        }
        return try {
            val directoryNode = getDirectoryNode(parentPath, create).getOrThrow()
            Result.success(
                if (create) directoryNode.getOrCreateFile(path.name)
                else directoryNode[path.name] as VfsFileNode
            )
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun move(oldPath: Path, newPath: Path) {
        if (oldPath == newPath) return
        getFileNode(oldPath, create = false).onSuccess { oldFileNode ->
            val newFileNode = getFileNode(newPath).getOrThrow()
            oldFileNode.useBuffer { oldBuffer ->
                newFileNode.useBuffer { newBuffer ->
                    oldBuffer.transferTo(newBuffer)
                }
            }
            delete(oldPath, false)
        }
    }

    override suspend fun delete(path: Path, mustExist: Boolean) {
        val resolvedPath = resolve(path)
        val parentPath = resolvedPath.parent ?: Path("")
        getDirectoryNode(parentPath).onSuccess { directoryNode ->
            directoryNode -= path.name
        }.onFailure {
            check(!mustExist) { "Cannot delete file $path as it does not exist" }
        }
    }

    override suspend fun createDirectories(path: Path, mustCreate: Boolean) {
        if (exists(path)) {
            if (mustCreate) error("Cannot create directory at $path")
            else return
        }
        getDirectoryNode(path, create = true)
    }

    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink {
        val node = getFileNode(path).getOrThrow()
        if (!append) node.useBuffer(Buffer::clear) // Clear file contents before writing to it again
        return VfsFileSink(node)
    }

    override suspend fun source(path: Path): AsyncRawSource {
        return VfsFileSource(getFileNode(path, create = false).getOrThrow())
    }

    override suspend fun exists(path: Path): Boolean = getFileNode(path, create = false).fold({ true }) {
        getDirectoryNode(path).fold({ true }) { false }
    }

    override suspend fun resolve(path: Path): Path {
        return if (path.isAbsolute) path.normalize()
        else (getWorkingDirectory() / path).normalize()
    }

    override suspend fun metadataOrNull(path: Path): FileMetadata? =
        getFileNode(path, create = false).fold({ fileNode ->
            FileMetadata( // @formatter:off
                isRegularFile = true,
                isDirectory = false,
                size = fileNode.useBuffer(Buffer::size)
            ) // @formatter:on
        }) {
            getDirectoryNode(path).fold({
                FileMetadata( // @formatter:off
                    isRegularFile = false,
                    isDirectory = true,
                    size = 0L
                ) // @formatter:on
            }) { null }
        }

    override suspend fun list(path: Path): List<Path> {
        val node = getDirectoryNode(path)
        if (node.isFailure) return emptyList()
        return node.getOrThrow().entries().map { node -> path / node.name }.toList()
    }

    override suspend fun close() {
        rootNode.clear()
    }

    override fun closeAbruptly() = Unit // If we close abruptly, we can't clean up after ourselves
}