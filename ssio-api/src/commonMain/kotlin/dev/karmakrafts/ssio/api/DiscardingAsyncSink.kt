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

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString

object DiscardingAsyncSink : AsyncSink {
    override suspend fun emit() = Unit

    override suspend fun writeByte(value: Byte) = Unit

    override suspend fun writeShort(value: Short) = Unit

    override suspend fun writeInt(value: Int) = Unit

    override suspend fun writeLong(value: Long) = Unit

    override suspend fun writeShortLe(value: Short) = Unit

    override suspend fun writeIntLe(value: Int) = Unit

    override suspend fun writeLongLe(value: Long) = Unit

    override suspend fun writeByteString(value: ByteString, startIndex: Int, endIndex: Int) = Unit

    override suspend fun writeByteArray(value: ByteArray, startIndex: Int, endIndex: Int) = Unit

    override suspend fun write(source: Buffer, byteCount: Long) = Unit

    override suspend fun flush() = Unit

    override suspend fun close() = Unit

    override fun closeAbruptly() = Unit
}