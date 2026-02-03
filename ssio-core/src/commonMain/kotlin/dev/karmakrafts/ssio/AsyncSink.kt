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

interface AsyncSink : AsyncRawSink {
    suspend fun writeByte(value: Byte)
    suspend fun writeShort(value: Short)
    suspend fun writeInt(value: Int)
    suspend fun writeLong(value: Long)
}

suspend fun AsyncSink.writeUByte(value: UByte) = writeByte(value.toByte())
suspend fun AsyncSink.writeUShort(value: UShort) = writeShort(value.toShort())
suspend fun AsyncSink.writeUInt(value: UInt) = writeInt(value.toInt())
suspend fun AsyncSink.writeULong(value: ULong) = writeLong(value.toLong())

suspend fun AsyncSink.writeFloat(value: Float) = writeInt(value.toBits())
suspend fun AsyncSink.writeDouble(value: Double) = writeLong(value.toBits())