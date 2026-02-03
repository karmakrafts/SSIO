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

internal abstract class AbstractAsyncFileSystem : AsyncFileSystem {
    companion object {
        private fun normalize(path: Path): Path {
            val normalized = ArrayDeque<String>()
            val segments = path.toString().split("/")
            for (segment in segments) {
                when (segment) {
                    "." -> {} // Ignore this
                    ".." -> normalized.removeLast()
                    else -> normalized += segment
                }
            }
            return Path("/${normalized.joinToString("/")}")
        }
    }

    override suspend fun resolve(path: Path): Path {
        return if (path.isAbsolute) normalize(path)
        else normalize(workingDirectory / path)
    }
}