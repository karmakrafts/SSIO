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
import kotlinx.io.RawSource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min

/**
 * A wrapper for a [RawSource] that implements [AsyncRawSource].
 */
private class AsyncRawSourceWrapper( // @formatter:off
    private val delegate: RawSource,
    private val chunkSize: Int
) : AsyncRawSource { // @formatter:on
    private val isClosed: AtomicBoolean = AtomicBoolean(false)

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed.load()) { "AsyncRawSource is already closed" }
        return withContext(ioDispatcher) {
            var remaining = byteCount
            var readTotal = 0L
            while (remaining > 0) {
                val toRead = min(chunkSize.toLong(), remaining)
                val read = delegate.readAtMostTo(sink, toRead)
                if (read == -1L) break
                readTotal += read
                remaining -= read
                yield() // Yield between chunks to keep things non-blocking
            }
            readTotal
        }
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "AsyncRawSource is already closed"
        }
        withContext(ioDispatcher) {
            delegate.close()
        }
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) {
            "AsyncRawSource is already closed"
        }
        delegate.close()
    }
}

/**
 * Wraps this [RawSource] as an [AsyncRawSource].
 *
 * @return an [AsyncRawSource] that delegates to this [RawSource].
 */
fun RawSource.asAsync(chunkSize: Int = 64): AsyncRawSource = AsyncRawSourceWrapper(this, chunkSize)