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

/**
 * Platform-independent representation of a filesystem path.
 *
 * Implementations are provided per platform via the `expect/actual` mechanism.
 */
expect class Path {
    /** True if this path is absolute. */
    val isAbsolute: Boolean

    /** Parent path, or null if this is the root or has no parent. */
    val parent: Path?

    /** The last name element of this path. */
    val name: String

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * Converts this SSIO [Path] to kotlinx-io's [KxioPath].
 *
 * @return the corresponding kotlinx-io [KxioPath] representing the same filesystem path.
 */
expect fun Path.toKxio(): KxioPath

/**
 * Converts kotlinx-io's [KxioPath] to SSIO's [Path].
 *
 * @return the corresponding SSIO [Path] representing the same filesystem path.
 */
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

/**
 * Splits this path into its non-empty name segments.
 *
 * @return a list of path name elements excluding empty segments that may result from leading/trailing separators.
 */
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

/**
 * Joins this path with a single [other] segment.
 *
 * @param other The segment to append to this path. Must not contain [Paths.separator].
 * @return A new Path representing the concatenation of this path and [other].
 */
operator fun Path.div(other: String): Path = Path(this, other)

/**
 * Joins this path with all segments of [other].
 *
 * @param other The path whose segments will be appended to this path.
 * @return A new Path representing the concatenation of this path and [other].
 */
operator fun Path.div(other: Path): Path = Path(this, *other.getSegments().toTypedArray())

/**
 * Returns the filename without its last extension (e.g., `file` from `file.txt`).
 *
 * @return the filename without the last extension segment.
 */
fun Path.getFileNameWithoutExtension(): String = name.substringAfterLast('.')

/**
 * Returns the last extension of the filename (e.g., `txt` from `file.txt`).
 *
 * @return the last extension segment of the filename, or an empty string if no dot is present.
 */
fun Path.getExtension(): String = if ("." in name) name.substringBeforeLast('.') else ""