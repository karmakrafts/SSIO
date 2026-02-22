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

package dev.karmakrafts.ssio.api

import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min

/**
 * A wrapper for a [RawSink] that implements [AsyncRawSink].
 */
private class AsyncRawSinkWrapper( // @formatter:off
    private val delegate: RawSink,
    private val chunkSize: Int
) : AsyncRawSink { // @formatter:on
    private val isClosed: AtomicBoolean = AtomicBoolean(false)

    override suspend fun write(source: Buffer, byteCount: Long) {
        check(!isClosed.load()) { "AsyncRawSink is already closed" }
        withContext(ioDispatcher) {
            var remaining = byteCount
            while (remaining > 0) {
                val toWrite = min(chunkSize.toLong(), remaining)
                delegate.write(source, toWrite)
                remaining -= toWrite
                yield() // Yield between chunks to keep things non-blocking
            }
        }
    }

    override suspend fun flush() = withContext(ioDispatcher) {
        check(!isClosed.load()) { "AsyncRawSink is already closed" }
        delegate.flush()
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "AsyncRawSink is already closed"
        }
        withContext(ioDispatcher) {
            delegate.close()
        }
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "AsyncRawSink is already closed"
        }
        delegate.close()
    }
}

/**
 * Wraps this [RawSink] as an [AsyncRawSink].
 *
 * @param chunkSize The size of the chunks emitted between yields in bytes.
 * @return an [AsyncRawSink] that delegates to this [RawSink].
 */
fun RawSink.asAsync(chunkSize: Int = 64): AsyncRawSink = AsyncRawSinkWrapper(this, chunkSize)