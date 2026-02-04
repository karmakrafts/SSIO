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

private object AsyncFileSystemImpl : AbstractAsyncFileSystem() {
    override suspend fun getWorkingDirectory(): Path {
        TODO("Not yet implemented")
    }

    override suspend fun getTempDirectory(): Path {
        TODO("Not yet implemented")
    }

    override suspend fun sink(path: Path, append: Boolean): AsyncRawSink {
        TODO("Not yet implemented")
    }

    override suspend fun source(path: Path): AsyncRawSource {
        TODO("Not yet implemented")
    }
}

actual val AsyncSystemFileSystem: AsyncFileSystem = AsyncFileSystemImpl