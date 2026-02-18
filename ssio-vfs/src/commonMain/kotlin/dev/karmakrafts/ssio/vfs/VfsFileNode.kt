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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer

internal data class VfsFileNode( // @formatter:off
    override val name: String,
    private val buffer: Buffer = Buffer()
) : VfsNode { // @formatter:on
    private val mutex: Mutex = Mutex()

    suspend inline fun <R> useBuffer(block: (Buffer) -> R): R = mutex.withLock {
        block(buffer)
    }
}