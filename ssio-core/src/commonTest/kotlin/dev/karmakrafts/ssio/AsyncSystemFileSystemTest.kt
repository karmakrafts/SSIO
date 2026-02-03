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

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AsyncSystemFileSystemTest {
    @Test
    fun `Resolve simple relative path`() = runTest {
        val relativePath = Path("./foo/bar/test.txt")
        val absolutePath = AsyncSystemFileSystem.resolve(relativePath)
        assertTrue(absolutePath.isAbsolute)
        assertTrue(absolutePath.toString().endsWith("foo/bar/test.txt"))
    }

    @Test
    fun `Resolve complex relative path`() = runTest {
        val relativePath = Path("./foo/bar/../test.txt")
        val absolutePath = AsyncSystemFileSystem.resolve(relativePath)
        assertTrue(absolutePath.isAbsolute)
        assertTrue(absolutePath.toString().endsWith("foo/test.txt"))
    }

    @Test
    fun `Resolve absolute path`() {

    }
}