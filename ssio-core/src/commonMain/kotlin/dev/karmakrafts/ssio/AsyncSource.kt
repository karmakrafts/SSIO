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

interface AsyncSource : AsyncRawSource {
    suspend fun await(predicate: AwaitPredicate): Result<Boolean>
    suspend fun awaitOrThrow(predicate: AwaitPredicate) = await(predicate).getOrThrow()

    suspend fun readByte(): Byte
    suspend fun readShort(): Short
    suspend fun readInt(): Int
    suspend fun readLong(): Long
}

suspend fun AsyncSource.readUByte(): UByte = readByte().toUByte()
suspend fun AsyncSource.readUShort(): UShort = readShort().toUShort()
suspend fun AsyncSource.readUInt(): UInt = readInt().toUInt()
suspend fun AsyncSource.readULong(): ULong = readLong().toULong()

suspend fun AsyncSource.readFloat(): Float = Float.fromBits(readInt())
suspend fun AsyncSource.readDouble(): Double = Double.fromBits(readLong())