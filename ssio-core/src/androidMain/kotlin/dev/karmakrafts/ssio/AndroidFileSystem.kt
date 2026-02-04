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

import android.content.Context
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference

object AndroidFileSystem {
    @PublishedApi
    internal val isInitialized: AtomicBoolean = AtomicBoolean(false)

    @PublishedApi
    internal val _context: AtomicReference<(() -> Context)?> = AtomicReference(null)

    inline val context: Context
        get() {
            check(isInitialized.load()) { "AndroidFileSystem is not initialized" }
            return _context.load()!!()
        }

    fun init(contextGetter: () -> Context) {
        check(isInitialized.compareAndSet(expectedValue = false, newValue = true)) {
            "AndroidFileSystem is already initialized"
        }
        _context.store(contextGetter)
    }
}