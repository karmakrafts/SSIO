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

import kotlinx.io.files.Path as KxioPath

actual class Path {
    var value: String = ""

    actual val isAbsolute: Boolean
        get() = value.startsWith(Paths.separator)

    actual val parent: Path?
        get() {
            if (Paths.separator !in value) return null
            return Path(value.substringBeforeLast(Paths.separator))
        }

    actual val name: String
        get() {
            return if (Paths.separator in value) value.substringAfterLast(Paths.separator)
            else value
        }

    actual override fun toString(): String = value
    actual override fun equals(other: Any?): Boolean = other is Path && value == other.value
    actual override fun hashCode(): Int = value.hashCode()
}

actual fun Path(path: String): Path = Path().apply {
    value = path
}

@DelicateSsioApi
actual fun Path.toKxio(): KxioPath = KxioPath(toString())

@DelicateSsioApi
actual fun KxioPath.toSsio(): Path = Path(toString())