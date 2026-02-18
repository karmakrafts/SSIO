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

import kotlinx.io.Sink
import kotlinx.io.writeIntLe

/** Writes a list size followed by elements encoded with [writer]. */
inline fun <T> Sink.writeList(list: List<T>, writer: Sink.(T) -> Unit) {
    writeInt(list.size)
    for (value in list) writer(value)
}

/** Writes a list size as little endian followed by elements encoded with [writer]. */
inline fun <T> Sink.writeListLe(list: List<T>, writer: Sink.(T) -> Unit) {
    writeIntLe(list.size)
    for (value in list) writer(value)
}

/** Writes a map size followed by key/value pairs encoded with [keyWriter] and [valueWriter]. */
inline fun <K, V> Sink.writeMap(map: Map<K, V>, keyWriter: Sink.(K) -> Unit, valueWriter: Sink.(V) -> Unit) {
    writeInt(map.size)
    for ((key, value) in map) {
        keyWriter(key)
        valueWriter(value)
    }
}

/** Writes a map size as little endian followed by key/value pairs encoded with [keyWriter] and [valueWriter]. */
inline fun <K, V> Sink.writeMapLe(map: Map<K, V>, keyWriter: Sink.(K) -> Unit, valueWriter: Sink.(V) -> Unit) {
    writeIntLe(map.size)
    for ((key, value) in map) {
        keyWriter(key)
        valueWriter(value)
    }
}