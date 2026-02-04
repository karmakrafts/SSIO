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

import kotlinx.io.Source

inline fun <T> Source.readList(reader: Source.() -> T): List<T> {
    val size = readInt()
    return if (size == 0) emptyList()
    else (0..<size).map { reader() }
}

inline fun <K, V> Source.readMap(keyReader: Source.() -> K, valueReader: Source.() -> V): Map<K, V> {
    val size = readInt()
    return if (size == 0) emptyMap()
    else (0..<size).associate { keyReader() to valueReader() }
}