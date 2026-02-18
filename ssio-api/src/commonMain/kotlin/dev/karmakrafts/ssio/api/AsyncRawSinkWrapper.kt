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

package dev.karmakrafts.ssio.api

import kotlinx.io.Buffer
import kotlinx.io.RawSink

private class AsyncRawSinkWrapper(
    private val delegate: RawSink
) : AsyncRawSink {
    override suspend fun write(source: Buffer, byteCount: Long) {
        delegate.write(source, byteCount)
    }

    override suspend fun flush() = delegate.flush()
    override suspend fun close() = delegate.close()
    override fun closeAbruptly() = delegate.close()
}

/** Wraps a synchronous [RawSink] into an [AsyncRawSink] facade. */
fun RawSink.asAsync(): AsyncRawSink = AsyncRawSinkWrapper(this)