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
import dev.karmakrafts.ssio.files.Paths
import dev.karmakrafts.ssio.files.div

internal abstract class AbstractAsyncFileSystem : AsyncFileSystem {
    companion object {
        private fun normalize(path: Path): Path {
            val normalized = ArrayDeque<String>()
            val segments = path.toString().split(Paths.separator)
            for (segment in segments) {
                when (segment) {
                    "." -> {} // Ignore this
                    ".." -> normalized.removeLast()
                    else -> normalized += segment
                }
            }
            return Path("${Paths.separator}${normalized.joinToString(Paths.separator)}")
        }
    }

    override suspend fun resolve(path: Path): Path {
        return if (path.isAbsolute) normalize(path)
        else normalize(getWorkingDirectory() / path)
    }
}