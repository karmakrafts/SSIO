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

package dev.karmakrafts.ssio.files

import kotlinx.io.files.Path as KxioPath

expect class Path {
    val isAbsolute: Boolean
    val parent: Path?
    val name: String

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

expect fun Path.toKxio(): KxioPath
expect fun KxioPath.toSsio(): Path

expect fun Path(path: String): Path

fun Path(base: Path, vararg segments: String): Path {
    val rawBase = base.toString()
    return Path(
        if (rawBase.isEmpty()) segments.joinToString(Paths.separator)
        else "$base${Paths.separator}${segments.joinToString(Paths.separator)}"
    )
}

fun Path(base: String, vararg segments: String): Path = Path(Path(base), *segments)

fun Path.getSegments(): List<String> = toString().split(Paths.separator).filterNot(String::isEmpty)

operator fun Path.div(other: String): Path = Path(this, other)
operator fun Path.div(other: Path): Path = Path(this, *other.getSegments().toTypedArray())