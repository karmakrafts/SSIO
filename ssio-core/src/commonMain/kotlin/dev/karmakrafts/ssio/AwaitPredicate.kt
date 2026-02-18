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
 * Predicate used by [AsyncSource.await] to decide when enough data is available.
 *
 * Implementations examine the current buffered [Buffer] and may request more data via
 * `fetchMore`. Returning true signals the awaiting condition is satisfied.
 */
fun interface AwaitPredicate {
    companion object {
        /** Predicate that evaluates to true only when the source is fully exhausted. */
        fun exhausted(): AwaitPredicate = { buffer, fetchMore -> buffer.exhausted() && !fetchMore() }

        /** Predicate that evaluates to true when at least [bytes] are available in the buffer. */
        fun available(bytes: Long): AwaitPredicate = { buffer, _ -> buffer.size >= bytes }
    }

    /**
     * Evaluates this predicate using current [buffer] state. Call [fetchMore] to attempt fetching
     * more data into the buffer; it returns true if additional data was fetched.
     */
    suspend operator fun invoke(buffer: Buffer, fetchMore: suspend () -> Boolean): Boolean
}