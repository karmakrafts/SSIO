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

/**
 * Create a new Path from the given path string.
 * The given string may be a relative or absolute path, and it may contain
 * elements like `.` and `..`, which will be retained.
 *
 * @param path The path string to create a new Path from.
 * @return A new Path instance containing the given path string.
 */
expect fun Path(path: String): Path

/**
 * Creates a new sub-path from the given base path and path segments.
 * The newly created sub-path will be normalized after concatenation.
 *
 * @param base The base path to create the new sub-path from.
 * @param segments The path segments to concatenate with the given base path.
 *  Segments must not contain [Paths.separator]. `.` and `..` are allowed as segments.
 * @return A new Path instance
 */
fun Path(base: Path, vararg segments: String): Path {
    val rawBase = base.toString()
    val cleanSegments = segments.filterNot(String::isEmpty)
    return Path(
        when {
            rawBase.isEmpty() -> cleanSegments.joinToString(Paths.separator)
            rawBase == Paths.separator -> "${Paths.separator}${cleanSegments.joinToString(Paths.separator)}"
            else -> "$base${Paths.separator}${cleanSegments.joinToString(Paths.separator)}"
        }
    )
}

/**
 * Creates a new sub-path from the given base path string and path segments.
 * The newly created sub-path will be normalized after concatenation.
 *
 * @param base The base path to create the new sub-path from.
 *  This may contain [Paths.separator], `.` and `..`.
 * @param segments The path segments to concatenate with the given base path.
 *  Segments must not contain [Paths.separator]. `.` and `..` are allowed as segments.
 * @return A new Path instance
 */
fun Path(base: String, vararg segments: String): Path = Path(Path(base), *segments)

fun Path.getSegments(): List<String> = toString().split(Paths.separator).filterNot(String::isEmpty)

/**
 * Normalizes this path by evaluating any `.` and `..` elements.
 * For an absolute path, this function shall always return an absolute path.
 * For a relative path, the same thing goes.
 *
 * @return The normalized form of this Path.
 */
fun Path.normalize(): Path {
    val normalized = ArrayDeque<String>()
    val segments = toString().split(Paths.separator).filterNot(String::isEmpty)
    for (segment in segments) {
        when (segment) {
            "." -> {} // Ignore this
            ".." -> normalized.removeLast()
            else -> normalized += segment
        }
    }
    return if (isAbsolute) Path("${Paths.separator}${normalized.joinToString(Paths.separator)}")
    else Path(normalized.joinToString(Paths.separator))
}

operator fun Path.div(other: String): Path = Path(this, other)
operator fun Path.div(other: Path): Path = Path(this, *other.getSegments().toTypedArray())

fun Path.getFileNameWithoutExtension(): String = name.substringAfterLast('.')
fun Path.getExtension(): String = name.substringBeforeLast('.')