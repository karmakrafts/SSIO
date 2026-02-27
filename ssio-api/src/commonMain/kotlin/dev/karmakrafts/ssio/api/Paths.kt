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

/**
 * Platform-specific path utilities and constants.
 */
expect object Paths {
    /** Filesystem path separator for the current platform. */
    val separator: String

    /**
     * Drive separator for absolute paths.
     * On platforms like Windows, this will be `":"`,
     * on all other platforms it will be null.
     */
    val driveSeparator: String?

    /**
     * Filesystem delimiter for indicating the current directory.
     * Will be `"."` on most platforms.
     */
    val currentDirectoryDelimiter: String

    /**
     * Filesystem delimiter for indicating the parent directory.
     * Will be `".."` on most platforms.
     */
    val parentDirectoryDelimiter: String

    /**
     * Standard delimiter used before file extensions.
     * Will be `"."` on most platforms.
     */
    val extensionDelimiter: String

    /** Root path of the current platform (e.g., `/` on Unix). */
    val root: Path
}