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
 * Asynchronous read-write filesystem abstraction.
 *
 * Extends [AsyncReadOnlyFileSystem] with mutating operations and write access.
 */
interface AsyncFileSystem : AsyncReadOnlyFileSystem {
    /**
     * Returns the process working directory.
     *
     * @return The process working directory.
     */
    suspend fun getWorkingDirectory(): Path

    /**
     * Returns a directory suitable for temporary files.
     *
     * @return A directory suitable for temporary files.
     */
    suspend fun getTempDirectory(): Path

    /**
     * Moves a file or directory from [oldPath] to [newPath].
     *
     * @param oldPath The path of the file or directory to move.
     * @param newPath The path where the file or directory should be moved to.
     */
    suspend fun move(oldPath: Path, newPath: Path)

    /**
     * Deletes a file or directory at [path].
     *
     * If [mustExist] is false, succeeds silently if absent.
     *
     * @param path The path of the file or directory to delete.
     * @param mustExist If true, fails if the file or directory does not exist.
     */
    suspend fun delete(path: Path, mustExist: Boolean = true)

    /**
     * Creates all directories along [path].
     *
     * If [mustCreate] is true, fails if already exists.
     *
     * @param path The path of the directories to create.
     * @param mustCreate If true, fails if the directories already exist.
     */
    suspend fun createDirectories(path: Path, mustCreate: Boolean = false)

    /**
     * Opens a raw sink for writing to [path].
     *
     * If [append] is true, appends to existing file.
     *
     * @param path The path of the file to open a sink for.
     * @param append If true, appends to the existing file.
     * @return An [AsyncRawSink] for writing to the file at [path].
     */
    suspend fun sink(path: Path, append: Boolean = false): AsyncRawSink
}