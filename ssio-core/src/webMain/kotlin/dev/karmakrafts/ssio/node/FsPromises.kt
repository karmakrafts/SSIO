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

package dev.karmakrafts.ssio.node

import dev.karmakrafts.ssio.files.Paths
import js.core.JsPrimitives.toKotlinString
import js.import.import
import js.objects.unsafeJso
import js.promise.Promise
import js.promise.await
import js.promise.catch
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsString
import kotlin.js.definedExternally
import kotlin.js.toArray
import kotlin.js.toBoolean
import kotlin.js.toJsArray
import kotlin.js.toJsBoolean
import kotlin.js.toJsString
import kotlin.js.unsafeCast

internal external interface ReadResult<B : JsAny> : JsAny {
    val bytesRead: Int
    val buffer: B
}

internal external interface WriteResult<B : JsAny> : JsAny {
    val bytesWritten: Int
    val buffer: B
}

internal external interface FileHandle : JsAny {
    fun <B : JsAny> read( // @formatter:off
        buffer: B,
        offset: Int = definedExternally,
        length: Int = definedExternally,
        position: Int = definedExternally
    ): Promise<ReadResult<B>> // @formatter:on

    fun <B : JsAny> write( // @formatter:off
        buffer: B,
        offset: Int = definedExternally,
        length: Int = definedExternally,
        position: Int = definedExternally
    ): Promise<WriteResult<B>> // @formatter:on

    fun sync(): Promise<Nothing?>

    fun close(): Promise<Nothing?>
}

private external interface FsStatOptions : JsAny {
    var bigint: Boolean
}

private external interface FsRmOptions : JsAny {
    var force: Boolean
    var maxRetries: Int
    var recursive: Boolean
    var retryDelay: Int
}

private external interface FsReaddirOptions : JsAny {
    var encoding: String
    var withFileTypes: Boolean
    var recursive: Boolean
}

private external interface FsMkdirOptions : JsAny {
    var recursive: Boolean
    var mode: String
}

internal external interface FsStats : JsAny {
    val isDirectory: Boolean
    val isFile: Boolean
    val size: Long
}

private external interface FsConstants : JsAny {
    val F_OK: String
}

private external interface FsDirEnt : JsAny {
    val isDirectory: Boolean
    val isFile: Boolean
    val name: String
    val parentPath: String
}

private external interface FsPromisesApi : JsAny {
    val constants: FsConstants

    fun open(path: String, mode: String): Promise<FileHandle>
    fun access(path: String, mode: String): Promise<Nothing?>
    fun rename(oldPath: String, newPath: String): Promise<Nothing?>
    fun stat(path: String, options: FsStatOptions = definedExternally): Promise<FsStats>
    fun rm(path: String, options: FsRmOptions = definedExternally): Promise<Nothing?>
    fun readdir(path: String, options: FsReaddirOptions = definedExternally): Promise<JsArray<JsAny>>
    fun mkdir(path: String, options: FsMkdirOptions = definedExternally): Promise<JsAny?>
}

internal object FsPromises {
    private val fs: SuspendLazy<FsPromisesApi> = SuspendLazy { import("fs/promises") }

    suspend fun open(path: String, mode: String): FileHandle {
        return fs.get().open(path, mode).await()
    }

    suspend fun access(path: String): Boolean {
        // @formatter:off
        return fs.get().access(path, fs.get().constants.F_OK)
            .then { true.toJsBoolean() }
            .catch { false.toJsBoolean() }
            .await()
            .toBoolean()
        // @formatter:on
    }

    suspend fun rename(oldPath: String, newPath: String): Boolean {
        // @formatter:off
        return fs.get().rename(oldPath, newPath)
            .then { true.toJsBoolean() }
            .catch { false.toJsBoolean() }
            .await()
            .toBoolean()
        // @formatter:on
    }

    suspend fun stat(path: String): FsStats {
        return fs.get().stat(path).await()
    }

    suspend fun rm(path: String, force: Boolean = false): Boolean {
        // @formatter:off
        return fs.get().rm(path, unsafeJso {
            this.force = force
        }).then { true.toJsBoolean() }
            .catch { false.toJsBoolean() }
            .await()
            .toBoolean()
        // @formatter:on
    }

    suspend fun readdir(path: String, withFileTypes: Boolean = true): Array<String> {
        // @formatter:off
        return fs.get().readdir(path, unsafeJso {
            this.withFileTypes = withFileTypes
        }).then { buffers ->
            buffers.toArray()
                .map { entryValue ->
                    val entry = entryValue.unsafeCast<FsDirEnt>()
                    "${entry.parentPath}${Paths.separator}${entry.name}".toJsString()
                }
                .toTypedArray()
                .toJsArray()
        }.catch {
            emptyArray<JsString>().toJsArray()
        }.await()
            .toArray()
            .map { string -> string.toKotlinString() }
            .toTypedArray()
        // @formatter:on
    }

    suspend fun mkdir(path: String, recursive: Boolean = true): Boolean {
        // @formatter:off
        return fs.get().mkdir(path, unsafeJso {
            this.recursive = recursive
        }).then { true.toJsBoolean() }
            .catch { false.toJsBoolean() }
            .await()
            .toBoolean()
        // @formatter:on
    }
}