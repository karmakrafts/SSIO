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

@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.karmakrafts.ssio.opfs

import dev.karmakrafts.ssio.AbstractAsyncFileSystem
import dev.karmakrafts.ssio.AsyncRawSink
import dev.karmakrafts.ssio.AsyncRawSource
import dev.karmakrafts.ssio.files.Path
import dev.karmakrafts.ssio.files.div
import dev.karmakrafts.ssio.files.getSegments
import js.disposable.use
import js.iterable.IteratorReturnResult
import js.objects.unsafeJso
import js.promise.await
import kotlinx.io.files.FileMetadata
import web.fs.FileSystemDirectoryHandle
import web.fs.FileSystemFileHandle
import web.fs.FileSystemHandle
import web.fs.createWritable
import web.fs.getDirectoryHandle
import web.fs.getFile
import web.fs.getFileHandle
import web.fs.removeEntry
import web.fs.seek
import web.fs.write
import web.navigator.navigator
import web.storage.getDirectory
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.unsafeCast

/**
 * Async filesystem for JS/WASM using OPFS APIs.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal object OpfsFileSystem : AbstractAsyncFileSystem() {
    override suspend fun getWorkingDirectory(): Path = Path("/")
    override suspend fun getTempDirectory(): Path = Path("/tmp")

    private suspend fun getDirectoryHandle(path: Path, create: Boolean = false): Result<FileSystemDirectoryHandle> {
        val resolvedPath = resolve(path)
        val root = navigator.storage.getDirectory()
        return if (resolvedPath.toString().isEmpty() || (resolvedPath.isAbsolute && resolvedPath.getSegments()
                .isEmpty())
        ) Result.success(root)
        else {
            var handle = root
            for (segment in path.getSegments()) {
                try {
                    handle = handle.getDirectoryHandle(segment, unsafeJso {
                        this.create = create
                    })
                } catch (error: Throwable) {
                    return Result.failure(error)
                }
            }
            Result.success(handle)
        }
    }

    private suspend fun getFileHandle(path: Path, create: Boolean = true): Result<FileSystemFileHandle> {
        val resolvedPath = resolve(path)
        val parentPath = resolvedPath.parent ?: return try {
            Result.success(navigator.storage.getDirectory().getFileHandle(path.name, unsafeJso {
                this.create = create
            }))
        } catch (error: Throwable) {
            Result.failure(error)
        }
        return try {
            val directoryHandle = getDirectoryHandle(parentPath, create).getOrThrow()
            Result.success(directoryHandle.getFileHandle(path.name, unsafeJso {
                this.create = create
            }))
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun list(path: Path): List<Path> {
        val handle = getDirectoryHandle(path)
        if (handle.isFailure) return emptyList()
        val iterator = handle.getOrThrow().values()
        val entries = ArrayList<Path>()
        while (true) {
            val result = iterator.next().await().unsafeCast<IteratorReturnResult<FileSystemHandle>>()
            if (result.done) break
            entries += resolve(path / result.value.name)
        }
        return entries
    }

    override suspend fun createDirectories(path: Path, mustCreate: Boolean) {
        if (exists(path)) {
            if (mustCreate) error("Cannot create directory at $path")
            else return
        }
        getDirectoryHandle(path, create = true)
    }

    override suspend fun metadataOrNull(path: Path): FileMetadata? =
        getFileHandle(path, create = false).fold({ fileHandle ->
            FileMetadata( // @formatter:off
            isRegularFile = true,
            isDirectory = false,
            size = fileHandle.getFile().size.toLong()
        ) // @formatter:on
        }) {
            getDirectoryHandle(path).fold({
                FileMetadata( // @formatter:off
                isRegularFile = false,
                isDirectory = true,
                size = 0L
            ) // @formatter:on
            }) { null }
        }

    override suspend fun source(path: Path): AsyncRawSource {
        return OpfsFileSource(getFileHandle(path, create = false).getOrThrow().getFile().stream().getReader())
    }

    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink {
        val handle = getFileHandle(path, create = !exists(path)).getOrThrow()
        val stream = handle.createWritable()
        if (append) { // If we append, seek to the tail of the file
            val file = handle.getFile()
            stream.seek(file.size)
        }
        return OpfsFileSink(stream)
    }

    override suspend fun exists(path: Path): Boolean = getFileHandle(path, create = false).fold({ true }) {
        getDirectoryHandle(path).fold({ true }) { false }
    }

    override suspend fun move(oldPath: Path, newPath: Path) {
        if (oldPath == newPath) return
        getFileHandle(oldPath, create = false).onSuccess { oldFileHandle ->
            val newFileHandle = getFileHandle(newPath).getOrThrow()
            newFileHandle.createWritable().use { writable ->
                writable.write(oldFileHandle)
            }
            delete(oldPath, false)
        }
    }

    override suspend fun delete(path: Path, mustExist: Boolean) {
        val resolvedPath = resolve(path)
        val parentPath = resolvedPath.parent ?: Path("")
        getDirectoryHandle(parentPath).onSuccess { directoryHandle ->
            directoryHandle.removeEntry(path.name)
        }.onFailure {
            check(!mustExist) { "Cannot delete file $path as it does not exist" }
        }
    }
}