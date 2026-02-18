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

package dev.karmakrafts.ssio

import kotlinx.io.Buffer

/**
 * Low-level asynchronous sink that writes raw bytes from a [Buffer].
 *
 * This interface provides minimal primitives used by higher-level [AsyncSink].
 * Implementations should be non-blocking and safe to call from coroutines.
 */
interface AsyncRawSink : AsyncCloseable {
    /**
     * Writes up to [byteCount] bytes from [source] into this sink.
     *
     * The call may write fewer bytes than requested. Callers should loop until all
     * desired bytes are written. Implementations must not modify unread bytes in [source]
     * beyond those consumed.
     */
    suspend fun write(source: Buffer, byteCount: Long)

    /**
     * Flushes any buffered data to the underlying destination.
     */
    suspend fun flush()
}