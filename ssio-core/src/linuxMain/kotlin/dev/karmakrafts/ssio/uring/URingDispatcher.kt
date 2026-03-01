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

import dev.karmakrafts.filament.Thread
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import platform.posix.atexit
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.fetchAndDecrement
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalForeignApi::class)
internal object URingDispatcher {
    const val RING_ENTRIES: Int = 256
    const val COMPLETION_BATCH_SIZE: Int = 16

    private val isRunning: AtomicBoolean = AtomicBoolean(true)
    private val ring: URing = URing(RING_ENTRIES.toUInt())
    private val sqMutex: Mutex = Mutex() // To serialize SQ access from coroutines

    private val _submissionsInFlight: AtomicInt = AtomicInt(0)
    private inline val submissionsInFlight: Int
        get() = _submissionsInFlight.load()

    private inline val canAcceptSubmissions: Boolean
        get() = submissionsInFlight < RING_ENTRIES

    private val thread: Thread = Thread {
        val completions = ArrayList<URingCompletionQueueEntry>(COMPLETION_BATCH_SIZE)
        while (isRunning.load()) {
            var completionCount = ring.peekCompletions(completions, COMPLETION_BATCH_SIZE)
            if (completionCount == 0) continue
            for (index in 0..<completionCount) {
                val completion = completions[index]
                val deferredRef = completion.getData()?.asStableRef<CompletableDeferred<Int>>() ?: continue
                _submissionsInFlight.fetchAndDecrement()
                deferredRef.get().complete(completion.getResult()) // Complete deferred task with CQE result
                deferredRef.dispose() // Unpin pointer to underlying Kotlin object so it can be GC'd
            }
            ring.advance(completions.size)
            Thread.yield()
        }
    }

    init {
        atexit(staticCFunction<Unit> {
            val self = URingDispatcher
            self.shutdown()
        })
    }

    private suspend fun waitUntilIdle() {
        while (!canAcceptSubmissions) yield()
    }

    suspend inline fun op(crossinline action: (URingSubmissionQueueEntry) -> Unit): Int {
        waitUntilIdle()
        _submissionsInFlight.fetchAndIncrement()
        val completable = CompletableDeferred<Int>()
        sqMutex.withLock {
            val submission = ring.createSubmission()
            action(submission)
            submission.setData(StableRef.create(completable).asCPointer())
            ring.submit() // TODO: split off submit so we can batch submissions
        }
        return completable.await()
    }

    fun shutdown() {
        if (!isRunning.compareAndSet(expectedValue = true, newValue = false)) return
        thread.join()
        ring.close()
    }
}