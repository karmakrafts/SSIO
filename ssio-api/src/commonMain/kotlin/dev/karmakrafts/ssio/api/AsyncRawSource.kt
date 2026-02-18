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

/**
 * Low-level asynchronous source that reads raw bytes into a [Buffer].
 *
 * This interface provides minimal primitives used by higher-level [AsyncSource].
 * Implementations should be non-blocking and safe to call from coroutines.
 */
interface AsyncRawSource : AsyncCloseable {
    /**
     * Reads up to [byteCount] bytes from this source into [sink].
     *
     * Returns the number of bytes read, which may be less than requested, or `-1`
     * to indicate end of stream when no more data is available.
     */
    suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long
}