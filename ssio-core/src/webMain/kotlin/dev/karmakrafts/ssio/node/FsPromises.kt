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

import js.promise.Promise
import js.promise.await
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.definedExternally
import kotlin.js.js

@Suppress("UNUSED_PARAMETER")
private fun <I : JsAny> import(name: String): Promise<I> = js("""import(name)""")

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

    fun close(): Promise<Nothing?>
}

private external interface FsPromisesApi : JsAny {
    fun open(path: String, mode: String): Promise<FileHandle>
}

internal object FsPromises {
    suspend fun open(path: String, mode: String): FileHandle {
        val fs = import<FsPromisesApi>("fs/promises").await()
        return fs.open(path, mode).await()
    }
}