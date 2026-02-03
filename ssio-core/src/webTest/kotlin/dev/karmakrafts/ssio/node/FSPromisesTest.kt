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

package dev.karmakrafts.ssio.node

import dev.karmakrafts.ssio.isNode
import js.buffer.ArrayBuffer
import js.buffer.toByteArray
import js.promise.await
import kotlinx.coroutines.test.runTest
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toJsString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalWasmJsInterop::class)
class FSPromisesTest {
    @Test
    fun `Open and close FileHandle`() = runTest {
        if (!isNode) {
            println("This is not NodeJS, skipping test")
            println()
            return@runTest
        }
        val handle = FsPromises.open("test_file.txt", "w")
        handle.close().await()
    }

    @Test
    fun `Write to and read from file`() = runTest {
        if (!isNode) {
            println("This is not NodeJS, skipping test")
            println()
            return@runTest
        }

        val expectedValue = "Hello, World!"

        var handle = FsPromises.open("test_file.txt", "w")
        val writeResult = handle.write(expectedValue.toJsString()).await()
        assertEquals(expectedValue.length, writeResult.bytesWritten)
        handle.close().await()

        handle = FsPromises.open("test_file.txt", "r")
        val readResult = handle.read(ArrayBuffer(128)).await()
        assertEquals(expectedValue.length, readResult.bytesRead)

        val expectedData = "Hello, World!".encodeToByteArray()
        for (b in expectedData) print("0x${b.toHexString()} ")
        println()

        val actualData = readResult.buffer.toByteArray().sliceArray(expectedData.indices)
        for (b in actualData) print("0x${b.toHexString()} ")
        println()

        assertTrue(expectedData.contentEquals(actualData))
        handle.close().await()
    }
}