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

package dev.karmakrafts.ssio.node

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SuspendLazy<T>(
    val initializer: suspend () -> T
) {
    private var value: T? = null
    private var isInitialized: Boolean = false
    private val mutex: Mutex = Mutex()

    suspend fun get(): T = mutex.withLock {
        return if (isInitialized) value!!
        else {
            val value = initializer()
            this.value = value
            isInitialized = true
            value
        }
    }
}