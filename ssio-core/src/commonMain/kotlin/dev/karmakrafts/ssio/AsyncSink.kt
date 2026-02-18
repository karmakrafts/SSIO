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

import kotlinx.io.bytestring.ByteString

/**
 * High-level asynchronous sink API for writing primitive values and byte sequences.
 *
 * Built on top of [AsyncRawSink], this interface adds convenient methods to write
 * numeric primitives and `ByteArray`/`ByteString` data. Implementations may buffer
 * writes and should be coroutine-friendly.
 */
interface AsyncSink : AsyncRawSink {
    /**
     * Forces any buffered data to be emitted to the underlying [AsyncRawSink]
     * without closing it.
     */
    suspend fun emit()

    /** Writes a single signed 8-bit value. */
    suspend fun writeByte(value: Byte)

    /** Writes a single signed 16-bit value, big-endian unless documented otherwise. */
    suspend fun writeShort(value: Short)

    /** Writes a single signed 32-bit value, big-endian unless documented otherwise. */
    suspend fun writeInt(value: Int)

    /** Writes a single signed 64-bit value, big-endian unless documented otherwise. */
    suspend fun writeLong(value: Long)

    /** Writes a [ByteString] slice in range [startIndex), [endIndex). */
    suspend fun writeByteString(value: ByteString, startIndex: Int = 0, endIndex: Int = value.size)

    /** Writes a [ByteArray] slice in range [startIndex), [endIndex). */
    suspend fun writeByteArray(value: ByteArray, startIndex: Int = 0, endIndex: Int = value.size)
}

/** Writes an unsigned 8-bit value. */
suspend inline fun AsyncSink.writeUByte(value: UByte) = writeByte(value.toByte())

/** Writes an unsigned 16-bit value. */
suspend inline fun AsyncSink.writeUShort(value: UShort) = writeShort(value.toShort())

/** Writes an unsigned 32-bit value. */
suspend inline fun AsyncSink.writeUInt(value: UInt) = writeInt(value.toInt())

/** Writes an unsigned 64-bit value. */
suspend inline fun AsyncSink.writeULong(value: ULong) = writeLong(value.toLong())

/** Writes a 32-bit IEEE-754 floating-point value. */
suspend inline fun AsyncSink.writeFloat(value: Float) = writeInt(value.toBits())

/** Writes a 64-bit IEEE-754 floating-point value. */
suspend inline fun AsyncSink.writeDouble(value: Double) = writeLong(value.toBits())

/** Writes a signed 16-bit value as little endian. */
suspend inline fun AsyncSink.writeShortLe(value: Short) = writeShort(value.reverseBytes())

/** Writes a signed 32-bit value as little endian. */
suspend inline fun AsyncSink.writeIntLe(value: Int) = writeInt(value.reverseBytes())

/** Writes a signed 64-bit value as little endian. */
suspend inline fun AsyncSink.writeLongLe(value: Long) = writeLong(value.reverseBytes())

/** Writes an unsigned 16-bit value as little endian. */
suspend inline fun AsyncSink.writeUShortLe(value: UShort) = writeShortLe(value.toShort())

/** Writes an unsigned 32-bit value as little endian. */
suspend inline fun AsyncSink.writeUIntLe(value: UInt) = writeIntLe(value.toInt())

/** Writes an unsigned 64-bit value as little endian. */
suspend inline fun AsyncSink.writeULongLe(value: ULong) = writeLongLe(value.toLong())

/** Writes a 32-bit IEEE-754 floating-point value as little endian. */
suspend inline fun AsyncSink.writeFloatLe(value: Float) = writeIntLe(value.toBits())

/** Writes a 64-bit IEEE-754 floating-point value as little endian. */
suspend inline fun AsyncSink.writeDoubleLe(value: Double) = writeLongLe(value.toBits())

/** Writes a UTF-8 encoded [value] without length prefix. */
suspend inline fun AsyncSink.writeString(value: String) {
    writeByteArray(value.encodeToByteArray())
}

/** Writes the length of [value] followed by its UTF-8 bytes. */
suspend inline fun AsyncSink.writePrefixedString(value: String) {
    writeInt(value.length)
    writeByteArray(value.encodeToByteArray())
}

/** Writes the length of [value] as little endian followed by its UTF-8 bytes. */
suspend inline fun AsyncSink.writePrefixedStringLe(value: String) {
    writeIntLe(value.length)
    writeByteArray(value.encodeToByteArray())
}

/** Writes a list size followed by elements encoded with [writer]. */
suspend inline fun <T> AsyncSink.writeList(list: List<T>, writer: AsyncSink.(T) -> Unit) {
    writeInt(list.size)
    for (value in list) writer(value)
}

/** Writes a list size as little endian followed by elements encoded with [writer]. */
suspend inline fun <T> AsyncSink.writeListLe(list: List<T>, writer: AsyncSink.(T) -> Unit) {
    writeIntLe(list.size)
    for (value in list) writer(value)
}

/** Writes a map size followed by key/value pairs encoded with [keyWriter] and [valueWriter]. */
suspend inline fun <K, V> AsyncSink.writeMap(
    map: Map<K, V>, keyWriter: AsyncSink.(K) -> Unit, valueWriter: AsyncSink.(V) -> Unit
) {
    writeInt(map.size)
    for ((key, value) in map) {
        keyWriter(key)
        valueWriter(value)
    }
}

/** Writes a map size as little endian followed by key/value pairs encoded with [keyWriter] and [valueWriter]. */
suspend inline fun <K, V> AsyncSink.writeMapLe(
    map: Map<K, V>, keyWriter: AsyncSink.(K) -> Unit, valueWriter: AsyncSink.(V) -> Unit
) {
    writeIntLe(map.size)
    for ((key, value) in map) {
        keyWriter(key)
        valueWriter(value)
    }
}