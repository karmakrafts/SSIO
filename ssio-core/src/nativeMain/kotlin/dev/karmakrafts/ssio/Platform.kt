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

import dev.karmakrafts.ssio.api.AsyncRawSink
import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.Path

internal expect fun platformSyncFd(fd: Int)
internal expect fun platformGetCwd(): String
internal expect fun platformGetTmpDir(): String

internal expect suspend fun createFileSource(path: Path): AsyncRawSource
internal expect suspend fun createFileSink(path: Path, append: Boolean): AsyncRawSink