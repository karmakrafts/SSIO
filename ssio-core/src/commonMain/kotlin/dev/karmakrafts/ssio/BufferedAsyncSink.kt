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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min

private open class BufferedAsyncSink( // @formatter:off
    protected val rawSink: AsyncRawSink,
    private val bufferSize: Long
) : AsyncSink { // @formatter:on
    protected val isClosed: AtomicBoolean = AtomicBoolean(false)
    protected val buffer: Buffer = Buffer()

    override suspend fun writeByte(value: Byte) {
        check(!isClosed.load()) { "AsyncSink is closed" }
        buffer.writeByte(value)
        if (buffer.size >= bufferSize) flush()
    }

    override suspend fun writeShort(value: Short) {
        check(!isClosed.load()) { "AsyncSink is closed" }
        buffer.writeShort(value)
        if (buffer.size >= bufferSize) flush()
    }

    override suspend fun writeInt(value: Int) {
        check(!isClosed.load()) { "AsyncSink is closed" }
        buffer.writeInt(value)
        if (buffer.size >= bufferSize) flush()
    }

    override suspend fun writeLong(value: Long) {
        check(!isClosed.load()) { "AsyncSink is closed" }
        buffer.writeLong(value)
        if (buffer.size >= bufferSize) flush()
    }

    override suspend fun write(source: Buffer, byteCount: Long) {
        check(!isClosed.load()) { "AsyncSink is closed" }
        var remaining = min(source.size, byteCount)
        while (remaining > 0) {
            val toWrite = min(remaining, Int.MAX_VALUE.toLong())
            buffer.write(source, toWrite)
            remaining -= toWrite
            if (buffer.size >= bufferSize) flush()
        }
    }

    override suspend fun flush() {
        check(!isClosed.load()) { "AsyncSink is closed" }
        if (buffer.size > 0) {
            rawSink.write(buffer, buffer.size)
            buffer.clear()
        }
        rawSink.flush()
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSink is already closed" }
        if (buffer.size > 0) {
            rawSink.write(buffer, buffer.size)
            buffer.clear()
        }
        rawSink.close()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSink is already closed" }
        buffer.clear()
        rawSink.closeAbruptly()
    }
}

private class SynchronizedBufferedAsyncSink(
    rawSink: AsyncRawSink, bufferSize: Long
) : BufferedAsyncSink(rawSink, bufferSize) {
    private val mutex: Mutex = Mutex()

    override suspend fun writeByte(value: Byte) = mutex.withLock {
        super.writeByte(value)
    }

    override suspend fun writeShort(value: Short) = mutex.withLock {
        super.writeShort(value)
    }

    override suspend fun writeInt(value: Int) = mutex.withLock {
        super.writeInt(value)
    }

    override suspend fun writeLong(value: Long) = mutex.withLock {
        super.writeLong(value)
    }

    override suspend fun write(source: Buffer, byteCount: Long) = mutex.withLock {
        super.write(source, byteCount)
    }

    override suspend fun flush() = mutex.withLock {
        super.flush()
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSink is already closed" }
        mutex.withLock {
            if (buffer.size > 0) {
                rawSink.write(buffer, buffer.size)
                buffer.clear()
            }
        }
        rawSink.flush()
        rawSink.close()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSink is already closed" }
        rawSink.closeAbruptly()
    }
}

fun AsyncRawSink.buffered( // @formatter:off
    bufferSize: Long = 8 * 1024,
    synchronized: Boolean = false
): AsyncSink { // @formatter:on
    return if (synchronized) SynchronizedBufferedAsyncSink(this, bufferSize)
    else BufferedAsyncSink(this, bufferSize)
}