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

package dev.karmakrafts.ssio.vfs

import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.buffered
import dev.karmakrafts.ssio.api.div
import dev.karmakrafts.ssio.api.readPrefixedString
import dev.karmakrafts.ssio.api.use
import dev.karmakrafts.ssio.api.writePrefixedString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsyncVirtualFileSystemTest {
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
        AsyncVirtualFileSystem().use { vfs ->
            val root = vfs.resolve(Path(""))
            val relativePath = Path("foo") / "bar" / "test.txt"
            val absolutePath = vfs.resolve(relativePath)
            assertTrue(absolutePath.isAbsolute)
            assertEquals(root / relativePath, absolutePath)
        }
    }

    @Test
    fun `Resolve complex relative path`() = runTest {
        AsyncVirtualFileSystem().use { vfs ->
            val relativePath = Path("foo") / "bar" / ".." / "test.txt"
            val absolutePath = vfs.resolve(relativePath)
            assertTrue(absolutePath.isAbsolute)
            assertTrue("bar" !in absolutePath.toString())
        }
    }

    @Test
    fun `Create file`() = runTest {
        AsyncVirtualFileSystem().use { vfs ->
            val path = Path("baz") / "test.txt"
            vfs.sink(path).use {}
            vfs.delete(path)
        }
    }

    @Test
    fun `Check if file exists`() = runTest {
        AsyncVirtualFileSystem().use { vfs ->
            val path = Path("foo") / "test.txt"
            vfs.delete(path, mustExist = false)
            assertFalse(vfs.exists(path))
            vfs.sink(path).use {}
            assertTrue(vfs.exists(path))
            vfs.delete(path)
        }
    }

    @Test
    fun `List files`() = runTest {
        AsyncVirtualFileSystem().use { vfs ->
            val dir = Path("foo") / "bar"
            val path = dir / "test.txt"
            vfs.delete(path, mustExist = false)
            var entries = vfs.list(dir)
            assertTrue(entries.isEmpty())
            vfs.sink(path).use {}
            entries = vfs.list(dir)
            assertTrue(path in entries)
            vfs.delete(path)
        }
    }

    @Test
    fun `Read and write file`() = runTest {
        AsyncVirtualFileSystem().use { vfs ->
            val path = Path("baz") / "test2.bin"
            vfs.sink(path).buffered().use { sink ->
                sink.writePrefixedString(testData)
            }
            vfs.source(path).buffered().use { source ->
                assertEquals(testData, source.readPrefixedString())
            }
            vfs.delete(path)
        }
    }
}