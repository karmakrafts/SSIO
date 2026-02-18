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
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.min

private class BufferedAsyncSource( // @formatter:off
    private val rawSource: AsyncRawSource,
    private val bufferSize: Long
) : AsyncSource { // @formatter:on
    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val buffer: Buffer = Buffer()

    @Suppress("UNREACHABLE_CODE")
    override suspend fun await(predicate: AwaitPredicate): Result<Boolean> {
        check(!isClosed.load()) { "AsyncSource is closed" }
        return try {
            while (true) {
                if (predicate(buffer) { rawSource.readAtMostTo(buffer, bufferSize) != -1L }) return Result.success(true)
                if (rawSource.readAtMostTo(buffer, bufferSize) == -1L) return Result.success(false)
            }
            Result.success(false)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun readByteArray(): ByteArray {
        check(await(AwaitPredicate.exhausted()).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readByteArray()
    }

    override suspend fun readByteArray(byteCount: Int): ByteArray {
        check(await(AwaitPredicate.available(byteCount.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readByteArray(byteCount)
    }

    override suspend fun readByteString(): ByteString {
        check(await(AwaitPredicate.exhausted()).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readByteString()
    }

    override suspend fun readByteString(byteCount: Int): ByteString {
        check(await(AwaitPredicate.available(byteCount.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readByteString(byteCount)
    }

    override suspend fun readByte(): Byte {
        check(await(AwaitPredicate.available(Byte.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readByte()
    }

    override suspend fun readShort(): Short {
        check(await(AwaitPredicate.available(Short.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readShort()
    }

    override suspend fun readInt(): Int {
        check(await(AwaitPredicate.available(Int.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readInt()
    }

    override suspend fun readLong(): Long {
        check(await(AwaitPredicate.available(Long.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return buffer.readLong()
    }

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed.load()) { "AsyncSource is closed" }
        if (buffer.size > 0) {
            val toRead = min(byteCount, buffer.size)
            sink.write(buffer, toRead)
            return toRead
        }
        return rawSource.readAtMostTo(sink, byteCount)
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSource is closed" }
        buffer.clear()
        rawSource.close()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSource is closed" }
        buffer.clear()
        rawSource.closeAbruptly()
    }
}

private class SynchronizedBufferedAsyncSource( // @formatter:off
    private val rawSource: AsyncRawSource,
    private val bufferSize: Long
) : AsyncSource { // @formatter:on
    private val isClosed: AtomicBoolean = AtomicBoolean(false)
    private val buffer: Buffer = Buffer()
    private val mutex: Mutex = Mutex()

    override suspend fun await(predicate: AwaitPredicate): Result<Boolean> {
        check(!isClosed.load()) { "AsyncSource is closed" }
        return try {
            while (!isClosed.load()) {
                if (mutex.withLock { predicate(buffer) { rawSource.readAtMostTo(buffer, bufferSize) != -1L } }) {
                    return Result.success(true)
                }
                if (mutex.withLock { rawSource.readAtMostTo(buffer, bufferSize) == -1L }) {
                    return Result.success(false)
                }
            }
            Result.success(false)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    override suspend fun readByteArray(): ByteArray {
        check(await(AwaitPredicate.exhausted()).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readByteArray()
        }
    }

    override suspend fun readByteArray(byteCount: Int): ByteArray {
        check(await(AwaitPredicate.available(byteCount.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readByteArray(byteCount)
        }
    }

    override suspend fun readByteString(byteCount: Int): ByteString {
        check(await(AwaitPredicate.available(byteCount.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readByteString(byteCount)
        }
    }

    override suspend fun readByteString(): ByteString {
        check(await(AwaitPredicate.exhausted()).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readByteString()
        }
    }

    override suspend fun readByte(): Byte {
        check(await(AwaitPredicate.available(Byte.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readByte()
        }
    }

    override suspend fun readShort(): Short {
        check(await(AwaitPredicate.available(Short.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readShort()
        }
    }

    override suspend fun readInt(): Int {
        check(await(AwaitPredicate.available(Int.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readInt()
        }
    }

    override suspend fun readLong(): Long {
        check(await(AwaitPredicate.available(Long.SIZE_BYTES.toLong())).getOrThrow()) { "AsyncSource is exhausted" }
        return mutex.withLock {
            buffer.readLong()
        }
    }

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(!isClosed.load()) { "AsyncSource is closed" }
        mutex.withLock {
            if (buffer.size > 0) {
                val toRead = min(byteCount, buffer.size)
                sink.write(buffer, toRead)
                return toRead
            }
        }
        return rawSource.readAtMostTo(sink, byteCount)
    }

    override suspend fun close() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSource is closed" }
        mutex.withLock {
            buffer.clear()
        }
        rawSource.close()
    }

    override fun closeAbruptly() {
        check(isClosed.compareAndSet(expectedValue = false, newValue = true)) { "AsyncSource is closed" }
        rawSource.closeAbruptly()
    }
}

/**
 * Returns an [AsyncSource] that buffers reads from this raw source.
 *
 * @param bufferSize maximum in-memory buffer size for prefetched bytes
 * @param synchronized if true, serializes access with a mutex for thread-safety
 */
fun AsyncRawSource.buffered( // @formatter:off
    bufferSize: Long = 8192,
    synchronized: Boolean = false
): AsyncSource { // @formatter:on
    return if (synchronized) SynchronizedBufferedAsyncSource(this, bufferSize)
    else BufferedAsyncSource(this, bufferSize)
}