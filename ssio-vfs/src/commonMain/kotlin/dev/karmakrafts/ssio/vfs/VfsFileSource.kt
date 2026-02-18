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

import dev.karmakrafts.ssio.api.AsyncRawSource
import kotlinx.io.Buffer
import kotlin.math.min

internal class VfsFileSource(
    private val node: VfsFileNode
) : AsyncRawSource {
    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        return node.useBuffer { sourceBuffer ->
            val toRead = min(sourceBuffer.size, byteCount)
            sink.write(sourceBuffer, toRead)
            toRead
        }
    }

    override suspend fun close() = Unit

    override fun closeAbruptly() = Unit
}