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

import kotlinx.io.Source
import kotlinx.io.readIntLe

/** Reads a list encoded as size followed by elements decoded with [reader]. */
inline fun <T> Source.readList(reader: Source.() -> T): List<T> {
    val size = readInt()
    return if (size == 0) emptyList()
    else (0..<size).map { reader() }
}

/** Reads a list encoded as size as little endian followed by elements decoded with [reader]. */
inline fun <T> Source.readListLe(reader: Source.() -> T): List<T> {
    val size = readIntLe()
    return if (size == 0) emptyList()
    else (0..<size).map { reader() }
}

/** Reads a map encoded as size followed by key/value pairs decoded by [keyReader] and [valueReader]. */
inline fun <K, V> Source.readMap(keyReader: Source.() -> K, valueReader: Source.() -> V): Map<K, V> {
    val size = readInt()
    return if (size == 0) emptyMap()
    else (0..<size).associate { keyReader() to valueReader() }
}

/** Reads a map encoded as size as little endian followed by key/value pairs decoded by [keyReader] and [valueReader]. */
inline fun <K, V> Source.readMapLe(keyReader: Source.() -> K, valueReader: Source.() -> V): Map<K, V> {
    val size = readIntLe()
    return if (size == 0) emptyMap()
    else (0..<size).associate { keyReader() to valueReader() }
}