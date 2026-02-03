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
import dev.karmakrafts.ssio.files.getSegments
import js.disposable.use
import js.iterable.IteratorReturnResult
import js.promise.await
import kotlinx.io.files.FileMetadata
import web.fs.FileSystemDirectoryHandle
import web.fs.FileSystemFileHandle
import web.fs.FileSystemGetDirectoryOptions
import web.fs.FileSystemGetFileOptions
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
internal object OPFSFileSystem : AbstractAsyncFileSystem() {
    override suspend fun getWorkingDirectory(): Path = Path("")
    override suspend fun getTempDirectory(): Path = Path("temp")

    private suspend fun getDirectoryHandle(path: Path, create: Boolean = false): FileSystemDirectoryHandle {
        val resolvedPath = resolve(path)
        val root = navigator.storage.getDirectory()
        return if (resolvedPath.toString().isEmpty() || (resolvedPath.isAbsolute && resolvedPath.getSegments()
                .isEmpty())
        ) root
        else {
            var handle = root
            for (segment in path.getSegments()) {
                handle = handle.getDirectoryHandle(segment, object : FileSystemGetDirectoryOptions {
                    override var create: Boolean? = create
                })
            }
            handle
        }
    }

    private suspend fun getFileHandle(path: Path, create: Boolean = true): FileSystemFileHandle {
        val resolvedPath = resolve(path)
        val parentPath = resolvedPath.parent ?: return navigator.storage.getDirectory().getFileHandle(path.name)
        val directoryHandle = getDirectoryHandle(parentPath, create)
        return directoryHandle.getFileHandle(path.name, object : FileSystemGetFileOptions {
            override var create: Boolean? = create
        })
    }

    override suspend fun list(path: Path): List<Path> {
        val handle = getDirectoryHandle(path)
        val iterator = handle.values()
        val entries = ArrayList<Path>()
        while (true) {
            val result = iterator.next().await().unsafeCast<IteratorReturnResult<FileSystemHandle>>()
            if (result.done) break
            entries += Path(result.value.name)
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

    override suspend fun metadataOrNull(path: Path): FileMetadata? {
        return try {
            val fileHandle = getFileHandle(path)
            FileMetadata( // @formatter:off
                isRegularFile = true,
                isDirectory = false,
                size = fileHandle.getFile().size.toLong()
            ) // @formatter:on
        } catch (_: Throwable) {
            try {
                getDirectoryHandle(path)
                FileMetadata( // @formatter:off
                    isRegularFile = false,
                    isDirectory = true,
                    size = 0L
                ) // @formatter:on
            } catch (_: Throwable) {
                null
            }
        }
    }

    override suspend fun source(path: Path): AsyncRawSource {
        val handle = getFileHandle(path)
        val file = handle.getFile()
        return OPFSFileSource( // @formatter:off
            reader = file.stream().getReader(),
            size = file.size.toLong()
        ) // @formatter:on
    }

    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink {
        val handle = getFileHandle(path, create = !exists(path))
        val stream = handle.createWritable()
        if (append) { // If we append, seek to the tail of the file
            val file = handle.getFile()
            stream.seek(file.size)
        }
        return OPFSFileSink(stream)
    }

    override suspend fun exists(path: Path): Boolean = try {
        getFileHandle(path, create = false)
        true
    } catch (_: Throwable) {
        try {
            getDirectoryHandle(path)
            true
        } catch (_: Throwable) {
            false
        }
    }

    override suspend fun move(oldPath: Path, newPath: Path) {
        if (oldPath == newPath) return
        val oldHandle = getFileHandle(oldPath)
        val newHandle = getFileHandle(newPath)
        newHandle.createWritable().use { writable ->
            writable.write(oldHandle)
        }
        delete(oldPath)
    }

    override suspend fun delete(path: Path) {
        val resolvedPath = resolve(path)
        val parentPath = resolvedPath.parent ?: Path("")
        try {
            val dirHandle = getDirectoryHandle(parentPath)
            dirHandle.removeEntry(path.name)
        } catch (_: Throwable) {/* SWALLOW */
        }
    }
}