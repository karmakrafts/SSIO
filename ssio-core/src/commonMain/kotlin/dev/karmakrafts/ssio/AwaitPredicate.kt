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

fun interface AwaitPredicate {
    companion object {
        fun exhausted(): AwaitPredicate = { buffer, fetchMore -> buffer.exhausted() && !fetchMore() }
        fun available(bytes: Long): AwaitPredicate = { buffer, _ -> buffer.size >= bytes }
    }

    suspend operator fun invoke(buffer: Buffer, fetchMore: suspend () -> Boolean): Boolean
}