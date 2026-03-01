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

package dev.karmakrafts.ssio.uring

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.free
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import liburing.io_uring
import liburing.io_uring_cq_advance
import liburing.io_uring_cqe
import liburing.io_uring_get_sqe
import liburing.io_uring_peek_batch_cqe
import liburing.io_uring_queue_exit
import liburing.io_uring_queue_init
import liburing.io_uring_submit

@OptIn(ExperimentalForeignApi::class)
internal value class URing(
    val address: CPointer<io_uring>
) : AutoCloseable {
    init {
        check(isUringAvailable) { "Cannot create URing instance when kernel doesn't support it" }
    }

    constructor(entries: UInt) : this(nativeHeap.alloc<io_uring>().apply {
        require(entries and 1U == 0U) { "URing entry count must be a power of 2" }
        check(io_uring_queue_init(entries, ptr, 0U) == 0) { "Could not initialize URing instance" }
    }.ptr)

    fun createSubmission(): URingSubmissionQueueEntry =
        URingSubmissionQueueEntry(checkNotNull(io_uring_get_sqe(address)))

    fun peekCompletions(completions: MutableList<URingCompletionQueueEntry>, maxCompletions: Int): Int = memScoped {
        val cqes = allocArray<CPointerVar<io_uring_cqe>>(maxCompletions)
        val count = io_uring_peek_batch_cqe(address, cqes, maxCompletions.toUInt()).toInt()
        for (index in 0..<count) {
            val entry = URingCompletionQueueEntry(checkNotNull(cqes[index]))
            if (index !in completions.indices) completions.add(index, entry)
            else completions[index] = entry
        }
        count
    }

    fun submit() = io_uring_submit(address)

    fun advance(completionCount: Int) = io_uring_cq_advance(address, completionCount.toUInt())

    override fun close() {
        io_uring_queue_exit(address)
        nativeHeap.free(address)
    }
}