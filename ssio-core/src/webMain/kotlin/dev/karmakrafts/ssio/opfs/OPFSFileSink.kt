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

import dev.karmakrafts.ssio.AsyncRawSink
import kotlinx.io.Buffer
import kotlinx.io.files.Path

internal class OPFSFileSink( // @formatter:off
    private val path: Path,
    private val append: Boolean
) : AsyncRawSink { // @formatter:on
    override suspend fun write(source: Buffer, byteCount: Long) {

    }

    override suspend fun flush() {
        TODO("Not yet implemented")
    }

    override suspend fun close() {
        TODO("Not yet implemented")
    }

    override fun closeAbruptly() {
        TODO("Not yet implemented")
    }
}