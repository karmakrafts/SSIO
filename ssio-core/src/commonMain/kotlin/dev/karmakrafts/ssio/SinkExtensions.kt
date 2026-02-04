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

import kotlinx.io.Sink

inline fun <T> Sink.writeList(list: List<T>, writer: (T) -> Unit) {
    writeInt(list.size)
    list.forEach(writer)
}

inline fun <K, V> Sink.writeMap(map: Map<K, V>, keyWriter: (K) -> Unit, valueWriter: (V) -> Unit) {
    writeInt(map.size)
    for ((key, value) in map) {
        keyWriter(key)
        valueWriter(value)
    }
}