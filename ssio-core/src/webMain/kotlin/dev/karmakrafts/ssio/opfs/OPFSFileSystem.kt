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

package dev.karmakrafts.ssio.opfs

import dev.karmakrafts.ssio.AbstractAsyncFileSystem
import dev.karmakrafts.ssio.AsyncRawSink
import dev.karmakrafts.ssio.AsyncRawSource
import js.disposable.use
import js.promise.await
import js.promise.catch
import kotlinx.io.files.Path
import web.fs.createWritable
import web.fs.getFileHandle
import web.fs.removeEntry
import web.fs.write
import web.navigator.navigator
import web.storage.getDirectory
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toBoolean
import kotlin.js.toJsBoolean

/**
 * Async filesystem for JS/WASM using OPFS APIs.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal object OPFSFileSystem : AbstractAsyncFileSystem() {
    override val workingDirectory: Path = Path("") // Working directory is always the OPFS root

    override suspend fun source(path: Path): AsyncRawSource = OPFSFileSource(path)
    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink = OPFSFileSink(path, append)

    override suspend fun exists(path: Path): Boolean {
        val root = navigator.storage.getDirectory()
        return root.getFileHandleAsync(path.toString())
            .then { true.toJsBoolean() }
            .catch { false.toJsBoolean() }
            .await()
            .toBoolean()
    }

    override suspend fun move(oldPath: Path, newPath: Path) {
        if (oldPath == newPath) return
        val root = navigator.storage.getDirectory()
        val oldHandle = root.getFileHandle(oldPath.toString())
        val newHandle = root.getFileHandle(newPath.toString())
        newHandle.createWritable().use { writable ->
            writable.write(oldHandle)
        }
        root.removeEntry(oldPath.toString())
    }

    override suspend fun delete(path: Path) {
        val root = navigator.storage.getDirectory()
        root.removeEntry(path.toString())
    }
}