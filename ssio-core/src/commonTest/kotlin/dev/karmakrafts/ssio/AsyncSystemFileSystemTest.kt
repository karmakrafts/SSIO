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
import dev.karmakrafts.ssio.files.div
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsyncSystemFileSystemTest {
    companion object {
        private val testData: String = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, 
            sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
            At vero eos et accusam et justo duo dolores et ea rebum. 
            Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. 
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, 
            sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. 
            At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, 
            no sea takimata sanctus est Lorem ipsum dolor sit amet.
        """.trimIndent()
    }

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
    fun `Create file`() = runTest {
        val path = Path("baz") / "test.txt"
        AsyncSystemFileSystem.sink(path).use {}
        AsyncSystemFileSystem.delete(path)
    }

    @Test
    fun `Check if file exists`() = runTest {
        val path = Path("foo") / "test.txt"
        AsyncSystemFileSystem.delete(path, mustExist = false)
        assertFalse(AsyncSystemFileSystem.exists(path))
        AsyncSystemFileSystem.sink(path).use {}
        assertTrue(AsyncSystemFileSystem.exists(path))
        AsyncSystemFileSystem.delete(path)
    }

    @Test
    fun `List files`() = runTest {
        val dir = Path("foo") / "bar"
        val path = dir / "test.txt"
        var entries = AsyncSystemFileSystem.list(dir)
        assertTrue(entries.isEmpty())
        AsyncSystemFileSystem.sink(path).use {}
        entries = AsyncSystemFileSystem.list(dir)
        assertTrue(AsyncSystemFileSystem.resolve(path) in entries)
        AsyncSystemFileSystem.delete(path)
    }

    @Test
    fun `Read and write file`() = runTest {
        val path = Path("baz") / "test2.bin"
        AsyncSystemFileSystem.sink(path).buffered().use { sink ->
            sink.writePrefixedString(testData)
        }
        AsyncSystemFileSystem.source(path).buffered().use { source ->
            assertEquals(testData, source.readPrefixedString())
        }
        AsyncSystemFileSystem.delete(path)
    }
}