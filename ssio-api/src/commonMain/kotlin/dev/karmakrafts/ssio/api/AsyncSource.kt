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

import kotlinx.io.bytestring.ByteString

/**
 * High-level asynchronous source API for reading primitive values and byte sequences.
 *
 * Built on top of [AsyncRawSource], this interface adds convenient methods to read
 * numeric primitives and `ByteArray`/`ByteString` data. Implementations may buffer
 * reads and should be coroutine-friendly.
 */
interface AsyncSource : AsyncRawSource {
    /**
     * Suspends until [predicate] returns true or end-of-stream is reached.
     * Returns a [Result] indicating success and whether the condition was met.
     */
    suspend fun await(predicate: AwaitPredicate): Result<Boolean>

    /** Same as [await] but throws on failures. */
    suspend fun awaitOrThrow(predicate: AwaitPredicate) = await(predicate).getOrThrow()

    /** Reads a single signed 8-bit value. */
    suspend fun readByte(): Byte

    /** Reads a single signed 16-bit value, big-endian unless documented otherwise. */
    suspend fun readShort(): Short

    /** Reads a single signed 32-bit value, big-endian unless documented otherwise. */
    suspend fun readInt(): Int

    /** Reads a single signed 64-bit value, big-endian unless documented otherwise. */
    suspend fun readLong(): Long

    /** Reads a single signed 16-bit value, little-endian unless documented otherwise. */
    suspend fun readShortLe(): Short

    /** Reads a single signed 32-bit value, little-endian unless documented otherwise. */
    suspend fun readIntLe(): Int

    /** Reads a single signed 64-bit value, little-endian unless documented otherwise. */
    suspend fun readLongLe(): Long

    /** Reads exactly [byteCount] bytes as a [ByteString]. */
    suspend fun readByteString(byteCount: Int): ByteString

    /** Reads all remaining bytes as a [ByteString]. */
    suspend fun readByteString(): ByteString

    /** Reads exactly [byteCount] bytes as a [ByteArray]. */
    suspend fun readByteArray(byteCount: Int): ByteArray

    /** Reads all remaining bytes as a [ByteArray]. */
    suspend fun readByteArray(): ByteArray
}

/** Reads an unsigned 8-bit value. */
suspend inline fun AsyncSource.readUByte(): UByte = readByte().toUByte()

/** Reads an unsigned 16-bit value. */
suspend inline fun AsyncSource.readUShort(): UShort = readShort().toUShort()

/** Reads an unsigned 32-bit value. */
suspend inline fun AsyncSource.readUInt(): UInt = readInt().toUInt()

/** Reads an unsigned 64-bit value. */
suspend inline fun AsyncSource.readULong(): ULong = readLong().toULong()

/** Reads a 32-bit IEEE-754 floating-point value. */
suspend inline fun AsyncSource.readFloat(): Float = Float.fromBits(readInt())

/** Reads a 64-bit IEEE-754 floating-point value. */
suspend inline fun AsyncSource.readDouble(): Double = Double.fromBits(readLong())

/** Reads an unsigned 16-bit value. */
suspend inline fun AsyncSource.readUShortLe(): UShort = readShortLe().toUShort()

/** Reads an unsigned 32-bit value. */
suspend inline fun AsyncSource.readUIntLe(): UInt = readIntLe().toUInt()

/** Reads an unsigned 64-bit value. */
suspend inline fun AsyncSource.readULongLe(): ULong = readLongLe().toULong()

/** Reads a 32-bit IEEE-754 floating-point value. */
suspend inline fun AsyncSource.readFloatLe(): Float = Float.fromBits(readIntLe())

/** Reads a 64-bit IEEE-754 floating-point value. */
suspend inline fun AsyncSource.readDoubleLe(): Double = Double.fromBits(readLongLe())

/** Reads all remaining bytes as UTF-8 text. */
suspend inline fun AsyncSource.readString(): String = readByteArray().decodeToString()

/** Reads a length-prefixed UTF-8 string (size first, then bytes). */
suspend inline fun AsyncSource.readPrefixedString(): String = readByteArray(readInt()).decodeToString()

/** Reads a length-prefixed UTF-8 string (size as little endian first, then bytes). */
suspend inline fun AsyncSource.readPrefixedStringLe(): String = readByteArray(readIntLe()).decodeToString()

/** Reads a list encoded as size followed by elements decoded with [reader]. */
suspend inline fun <T> AsyncSource.readList(reader: AsyncSource.() -> T): List<T> {
    val size = readInt()
    return if (size == 0) emptyList()
    else (0..<size).map { reader() }
}

/** Reads a list encoded as size as little endian followed by elements decoded with [reader]. */
suspend inline fun <T> AsyncSource.readListLe(reader: AsyncSource.() -> T): List<T> {
    val size = readIntLe()
    return if (size == 0) emptyList()
    else (0..<size).map { reader() }
}

/** Reads a map encoded as size followed by key/value pairs decoded by [keyReader] and [valueReader]. */
suspend inline fun <K, V> AsyncSource.readMap(
    keyReader: AsyncSource.() -> K, valueReader: AsyncSource.() -> V
): Map<K, V> {
    val size = readInt()
    return if (size == 0) emptyMap()
    else (0..<size).associate { keyReader() to valueReader() }
}

/** Reads a map encoded as size as little endian followed by key/value pairs decoded by [keyReader] and [valueReader]. */
suspend inline fun <K, V> AsyncSource.readMapLe(
    keyReader: AsyncSource.() -> K, valueReader: AsyncSource.() -> V
): Map<K, V> {
    val size = readIntLe()
    return if (size == 0) emptyMap()
    else (0..<size).associate { keyReader() to valueReader() }
}