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

import dev.karmakrafts.ssio.api.AsyncSink
import dev.karmakrafts.ssio.api.AsyncSource
import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.buffered
import dev.karmakrafts.ssio.api.readDouble
import dev.karmakrafts.ssio.api.readDoubleLe
import dev.karmakrafts.ssio.api.readFloat
import dev.karmakrafts.ssio.api.readFloatLe
import dev.karmakrafts.ssio.api.readList
import dev.karmakrafts.ssio.api.readListLe
import dev.karmakrafts.ssio.api.readMap
import dev.karmakrafts.ssio.api.readMapLe
import dev.karmakrafts.ssio.api.readPrefixedString
import dev.karmakrafts.ssio.api.readPrefixedStringLe
import dev.karmakrafts.ssio.api.readString
import dev.karmakrafts.ssio.api.readUByte
import dev.karmakrafts.ssio.api.readUInt
import dev.karmakrafts.ssio.api.readUIntLe
import dev.karmakrafts.ssio.api.readULong
import dev.karmakrafts.ssio.api.readULongLe
import dev.karmakrafts.ssio.api.readUShort
import dev.karmakrafts.ssio.api.readUShortLe
import dev.karmakrafts.ssio.api.use
import dev.karmakrafts.ssio.api.writeDouble
import dev.karmakrafts.ssio.api.writeDoubleLe
import dev.karmakrafts.ssio.api.writeFloat
import dev.karmakrafts.ssio.api.writeFloatLe
import dev.karmakrafts.ssio.api.writeList
import dev.karmakrafts.ssio.api.writeListLe
import dev.karmakrafts.ssio.api.writeMap
import dev.karmakrafts.ssio.api.writeMapLe
import dev.karmakrafts.ssio.api.writePrefixedString
import dev.karmakrafts.ssio.api.writePrefixedStringLe
import dev.karmakrafts.ssio.api.writeString
import dev.karmakrafts.ssio.api.writeUByte
import dev.karmakrafts.ssio.api.writeUInt
import dev.karmakrafts.ssio.api.writeUIntLe
import dev.karmakrafts.ssio.api.writeULong
import dev.karmakrafts.ssio.api.writeULongLe
import dev.karmakrafts.ssio.api.writeUShort
import dev.karmakrafts.ssio.api.writeUShortLe
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AsyncSystemFileSystemReadWriteTest {
    private suspend inline fun <T> testReadWrite( // @formatter:off
        value: T, 
        crossinline write: suspend AsyncSink.(T) -> Unit,
        crossinline read: suspend AsyncSource.() -> T,
        crossinline asserter: (T, T) -> Unit
    ) { // @formatter:on
        val path = Path("test_extensions_${value.hashCode()}.bin")
        try {
            AsyncSystemFileSystem.sink(path).buffered().use { it.write(value) }
            val readValue = AsyncSystemFileSystem.source(path).buffered().use { it.read() }
            asserter(value, readValue)
        }
        finally {
            AsyncSystemFileSystem.delete(path, mustExist = false)
        }
    }

    @Test
    fun `Read and write Byte`() = runTest {
        testReadWrite(0xAB.toByte(), AsyncSink::writeByte, AsyncSource::readByte, ::assertEquals)
    }

    @Test
    fun `Read and write Short`() = runTest {
        testReadWrite(0x1234.toShort(), AsyncSink::writeShort, AsyncSource::readShort, ::assertEquals)
    }

    @Test
    fun `Read and write Int`() = runTest {
        testReadWrite(0x12345678, AsyncSink::writeInt, AsyncSource::readInt, ::assertEquals)
    }

    @Test
    fun `Read and write Long`() = runTest {
        testReadWrite(0x1234567890ABCDEF, AsyncSink::writeLong, AsyncSource::readLong, ::assertEquals)
    }

    @Test
    fun `Read and write ShortLe`() = runTest {
        testReadWrite(0x1234.toShort(), AsyncSink::writeShortLe, AsyncSource::readShortLe, ::assertEquals)
    }

    @Test
    fun `Read and write IntLe`() = runTest {
        testReadWrite(0x12345678, AsyncSink::writeIntLe, AsyncSource::readIntLe, ::assertEquals)
    }

    @Test
    fun `Read and write LongLe`() = runTest {
        testReadWrite(0x1234567890ABCDEF, AsyncSink::writeLongLe, AsyncSource::readLongLe, ::assertEquals)
    }

    @Test
    fun `Read and write ByteString`() = runTest {
        val bs = ByteString(1, 2, 3, 4, 5)
        testReadWrite(bs, AsyncSink::writeByteString, AsyncSource::readByteString, ::assertEquals)
    }

    @Test
    fun `Read and write ByteArray`() = runTest {
        val ba = byteArrayOf(1, 2, 3, 4, 5)
        testReadWrite(ba, AsyncSink::writeByteArray, AsyncSource::readByteArray, ::assertContentEquals)
    }

    @Test
    fun `Read and write UByte`() = runTest {
        testReadWrite(250.toUByte(), AsyncSink::writeUByte, AsyncSource::readUByte, ::assertEquals)
    }

    @Test
    fun `Read and write UShort`() = runTest {
        testReadWrite(60000.toUShort(), AsyncSink::writeUShort, AsyncSource::readUShort, ::assertEquals)
    }

    @Test
    fun `Read and write UInt`() = runTest {
        testReadWrite(4000000000.toUInt(), AsyncSink::writeUInt, AsyncSource::readUInt, ::assertEquals)
    }

    @Test
    fun `Read and write ULong`() = runTest {
        testReadWrite(18000000000000000000uL, AsyncSink::writeULong, AsyncSource::readULong, ::assertEquals)
    }

    @Test
    fun `Read and write Float`() = runTest {
        testReadWrite(3.14f, AsyncSink::writeFloat, AsyncSource::readFloat) { ex, ac ->
            assertEquals(ex, ac, 0.0001F)
        }
    }

    @Test
    fun `Read and write Double`() = runTest {
        testReadWrite(3.1415926535, AsyncSink::writeDouble, AsyncSource::readDouble) { ex, ac ->
            assertEquals(ex, ac, 0.0000001)
        }
    }

    @Test
    fun `Read and write UShortLe`() = runTest {
        testReadWrite(60000.toUShort(), AsyncSink::writeUShortLe, AsyncSource::readUShortLe, ::assertEquals)
    }

    @Test
    fun `Read and write UIntLe`() = runTest {
        testReadWrite(4000000000.toUInt(), AsyncSink::writeUIntLe, AsyncSource::readUIntLe, ::assertEquals)
    }

    @Test
    fun `Read and write ULongLe`() = runTest {
        testReadWrite(18000000000000000000uL, AsyncSink::writeULongLe, AsyncSource::readULongLe, ::assertEquals)
    }

    @Test
    fun `Read and write FloatLe`() = runTest {
        testReadWrite(3.14f, AsyncSink::writeFloatLe, AsyncSource::readFloatLe) { ex, ac ->
            assertEquals(ex, ac, 0.0001F)
        }
    }

    @Test
    fun `Read and write DoubleLe`() = runTest {
        testReadWrite(3.1415926535, AsyncSink::writeDoubleLe, AsyncSource::readDoubleLe) { ex, ac ->
            assertEquals(ex, ac, 0.0000001)
        }
    }

    @Test
    fun `Read and write String`() = runTest {
        testReadWrite("Hello, World!", AsyncSink::writeString, AsyncSource::readString, ::assertEquals)
    }

    @Test
    fun `Read and write PrefixedString`() = runTest {
        testReadWrite("Hello, World!", AsyncSink::writePrefixedString, AsyncSource::readPrefixedString, ::assertEquals)
    }

    @Test
    fun `Read and write PrefixedStringLe`() = runTest {
        testReadWrite(
            "Hello, World!",
            AsyncSink::writePrefixedStringLe,
            AsyncSource::readPrefixedStringLe,
            ::assertEquals
        )
    }

    @Test
    fun `Read and write List`() = runTest {
        val list = listOf("one", "two", "three")
        testReadWrite(
            list,
            { list -> writeList(list) { writePrefixedString(it) } },
            { readList { readPrefixedString() } },
            ::assertEquals
        )
    }

    @Test
    fun `Read and write ListLe`() = runTest {
        val list = listOf("one", "two", "three")
        testReadWrite(
            list,
            { list -> writeListLe(list) { writePrefixedString(it) } },
            { readListLe { readPrefixedString() } },
            ::assertEquals
        )
    }

    @Test
    fun `Read and write Map`() = runTest {
        val map = mapOf(1 to "one", 2 to "two")
        testReadWrite(
            map,
            { map -> writeMap(map, { writeInt(it) }, { writePrefixedString(it) }) },
            { readMap({ readInt() }, { readPrefixedString() }) },
            ::assertEquals
        )
    }

    @Test
    fun `Read and write MapLe`() = runTest {
        val map = mapOf(1 to "one", 2 to "two")
        testReadWrite(
            map,
            { map -> writeMapLe(map, { writeIntLe(it) }, { writePrefixedString(it) }) },
            { readMapLe({ readIntLe() }, { readPrefixedString() }) },
            ::assertEquals
        )
    }
}
