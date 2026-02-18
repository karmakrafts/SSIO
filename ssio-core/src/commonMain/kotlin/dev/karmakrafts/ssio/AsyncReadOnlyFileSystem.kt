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

import dev.karmakrafts.ssio.files.Path
import kotlinx.io.files.FileMetadata

/**
 * Asynchronous read-only filesystem abstraction.
 *
 * Provides non-blocking primitives for reading files and querying filesystem state.
 */
interface AsyncReadOnlyFileSystem {
    /**
     * Opens a raw source for reading file contents at [path].
     *
     * @param path the path to the file to open.
     * @return an [AsyncRawSource] for reading from the file.
     */
    suspend fun source(path: Path): AsyncRawSource

    /**
     * Returns true if a file or directory exists at [path].
     *
     * @param path the path to check for existence.
     * @return `true` if the path exists, `false` otherwise.
     */
    suspend fun exists(path: Path): Boolean

    /**
     * Resolves [path] to an absolute/normalized path in this filesystem.
     *
     * @param path the path to resolve.
     * @return the resolved [Path].
     */
    suspend fun resolve(path: Path): Path

    /**
     * Returns metadata for [path], or null if it doesn't exist.
     *
     * @param path the path to query metadata for.
     * @return a [FileMetadata] instance if the path exists, or `null` otherwise.
     */
    suspend fun metadataOrNull(path: Path): FileMetadata?

    /**
     * Lists entries inside the directory at [path].
     *
     * @param path the path to the directory to list entries from.
     * @return a [List] of [Path] objects representing the entries in the directory.
     */
    suspend fun list(path: Path): List<Path>
}