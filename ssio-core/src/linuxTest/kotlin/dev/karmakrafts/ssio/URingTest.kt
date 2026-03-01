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

import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.div
import dev.karmakrafts.ssio.api.use
import dev.karmakrafts.ssio.cio.NativeFile
import dev.karmakrafts.ssio.uring.URing
import dev.karmakrafts.ssio.uring.URingCompletionQueueEntry
import dev.karmakrafts.ssio.uring.isUringAvailable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import platform.posix.strerror
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
class URingTest {
    @Test
    fun `Check if available`() {
        assertTrue(isUringAvailable)
    }

    @Test
    fun `Read and write file`() = runTest {
        val testData = "Hello, World!"
        val path = Path("baz") / "uring_test.txt"
        val ring = URing(8U)
        // Write a single file
        NativeFile.create(path, writable = true, append = false).use { file ->
            val data = testData.encodeToByteArray()
            val pinnedData = data.pin()
            ring.createSubmission().prepareWrite(file.fd, pinnedData.addressOf(0), data.size.toUInt())
            ring.submit()
            val completions = ArrayList<URingCompletionQueueEntry>(4)
            var completionCount = ring.peekCompletions(completions, 4)
            while (completionCount == 0) {
                yield()
                completionCount = ring.peekCompletions(completions, 4)
            }
            for (index in 0..<completionCount) {
                val completion = completions[index]
                val rawCompletion = completion.address.pointed
                val result = rawCompletion.res
                assert(result >= 0) { "Error while writing file: ${strerror(result)?.toKStringFromUtf8()}" }
            }
            ring.advance(completionCount)
            pinnedData.unpin()
        }
        // Read a single file
        NativeFile.create(path).use { file ->
            val buffer = ByteArray(4096)
            val pinnedBuffer = buffer.pin()
            ring.createSubmission().prepareRead(file.fd, pinnedBuffer.addressOf(0), buffer.size.toUInt())
            ring.submit()
            val completions = ArrayList<URingCompletionQueueEntry>(4)
            var completionCount = ring.peekCompletions(completions, 4)
            while (completionCount == 0) {
                yield()
                completionCount = ring.peekCompletions(completions, 4)
            }
            var readTotal = 0L
            for (index in 0..<completionCount) {
                val completion = completions[index]
                val rawCompletion = completion.address.pointed
                val result = rawCompletion.res
                assert(result >= 0) { "Error while reading file: ${strerror(result)?.toKStringFromUtf8()}" }
                readTotal += result
            }
            ring.advance(completionCount)
            assertEquals(testData, buffer.sliceArray(0..<readTotal.toInt()).decodeToString())
            pinnedBuffer.unpin()
        }
        ring.close()
    }
}