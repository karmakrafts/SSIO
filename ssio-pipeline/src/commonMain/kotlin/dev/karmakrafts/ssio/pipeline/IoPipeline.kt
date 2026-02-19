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

package dev.karmakrafts.ssio.pipeline

import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.ExperimentalSsioApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlin.math.min

@ExperimentalSsioApi
interface IoPipeline : AsyncRawSource {
    fun transform(transform: IoTransform): IoPipeline
    suspend fun <R> mapAndClose(transform: Data2ObjectTransform<R>): R
}

@ExperimentalSsioApi
private data class IoPipelineImpl(
    private val source: AsyncRawSource
) : IoPipeline {
    companion object {
        private const val CHUNK_SIZE: Int = 4096
    }

    private val backBuffer: Buffer = Buffer()
    private val frontBuffer: Buffer = Buffer()
    private var opIndex: Int = 0
    private val transforms: ArrayList<IoTransform> = ArrayList(8)

    private inline val currentInput: Buffer
        get() = if (opIndex and 1 == 0) backBuffer else frontBuffer

    private inline val currentOutput: Buffer
        get() = if (opIndex and 1 == 1) backBuffer else frontBuffer

    override fun transform(transform: IoTransform): IoPipeline {
        transforms += transform
        return this
    }

    override suspend fun <R> mapAndClose(transform: Data2ObjectTransform<R>): R {
        return try {
            transform(currentOutput)
        }
        finally {
            withContext(NonCancellable) {
                close()
            }
        }
    }

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        var remaining = byteCount
        var readTotal = 0L
        while (remaining > 0) {
            val toRead = min(remaining, CHUNK_SIZE.toLong())
            opIndex = 0
            if (source.readAtMostTo(currentInput, toRead) == -1L) break
            for (transform in transforms) {
                currentOutput.clear()
                transform(currentInput, currentOutput)
                opIndex++
            }
            val chunkSize = currentOutput.size
            remaining -= chunkSize
            readTotal += chunkSize
            currentOutput.transferTo(sink)
        }
        return readTotal
    }

    override suspend fun close() {
        source.close()
        backBuffer.clear()
        frontBuffer.clear()
    }

    override fun closeAbruptly() {
        source.closeAbruptly()
        backBuffer.clear()
        frontBuffer.clear()
    }
}

@ExperimentalSsioApi
fun AsyncRawSource.asPipeline(): IoPipeline = IoPipelineImpl(this)