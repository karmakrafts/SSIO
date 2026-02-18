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
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Clock

object RandomAsyncSource : AsyncRawSource {
    private const val CHUNK_SIZE: Int = 8192

    private class Context( // @formatter:off
        val random: Random = Random(Clock.System.now().epochSeconds),
        val buffer: ByteArray = ByteArray(CHUNK_SIZE)
    ) // @formatter:on

    private val context: Context by ThreadLocal(::Context)

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val buffer = context.buffer
        var remaining = byteCount
        var writtenTotal = 0L
        while (remaining > 0L) {
            val chunkSize = min(Int.MAX_VALUE.toLong(), remaining).toInt()
            context.random.nextBytes(buffer, 0, chunkSize)
            sink.write(buffer, 0, chunkSize)
            remaining -= chunkSize
            writtenTotal += chunkSize
        }
        return writtenTotal
    }

    override suspend fun close() = Unit

    override fun closeAbruptly() = Unit
}