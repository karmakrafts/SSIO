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

import kotlinx.io.files.Path

interface AsyncFileSystem {
    suspend fun source(path: Path): AsyncRawSource
    suspend fun sink(path: Path, append: Boolean = false): AsyncRawSink
    suspend fun exists(path: Path): Boolean
    suspend fun atomicMove(oldPath: Path, newPath: Path)
    suspend fun delete(path: Path)
}

expect val AsyncSystemFileSystem: AsyncFileSystem