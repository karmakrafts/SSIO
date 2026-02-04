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
import kotlinx.io.bytestring.decodeToString

interface AsyncSource : AsyncRawSource {
    suspend fun await(predicate: AwaitPredicate): Result<Boolean>
    suspend fun awaitOrThrow(predicate: AwaitPredicate) = await(predicate).getOrThrow()

    suspend fun readByte(): Byte
    suspend fun readShort(): Short
    suspend fun readInt(): Int
    suspend fun readLong(): Long

    suspend fun readByteString(byteCount: Int): ByteString
    suspend fun readByteString(): ByteString
}

suspend fun AsyncSource.readUByte(): UByte = readByte().toUByte()
suspend fun AsyncSource.readUShort(): UShort = readShort().toUShort()
suspend fun AsyncSource.readUInt(): UInt = readInt().toUInt()
suspend fun AsyncSource.readULong(): ULong = readLong().toULong()

suspend fun AsyncSource.readFloat(): Float = Float.fromBits(readInt())
suspend fun AsyncSource.readDouble(): Double = Double.fromBits(readLong())

suspend fun AsyncSource.readString(): String = readByteString().decodeToString()
suspend fun AsyncSource.readPrefixedString(): String = readByteString(readInt()).decodeToString()

suspend inline fun <T> AsyncSource.readList(reader: AsyncSource.() -> T): List<T> {
    val size = readInt()
    return if (size == 0) emptyList()
    else (0..<size).map { reader() }
}

suspend inline fun <K, V> AsyncSource.readMap(
    keyReader: AsyncSource.() -> K, valueReader: AsyncSource.() -> V
): Map<K, V> {
    val size = readInt()
    return if (size == 0) emptyMap()
    else (0..<size).associate { keyReader() to valueReader() }
}