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

import dev.karmakrafts.ssio.api.AsyncCloseable
import dev.karmakrafts.ssio.api.AsyncRawSource
import dev.karmakrafts.ssio.api.ExperimentalSsioApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer

@ExperimentalSsioApi
class IoPipeline internal constructor( // @formatter:off
    private val source: AsyncRawSource,
    private val elements: List<IoPipelineElement>
) : AsyncRawSource, AsyncCloseable { // @formatter:on
    internal val backBuffer: Buffer = Buffer()
    internal val frontBuffer: Buffer = Buffer()
    internal var stageIndex: Int = 0
    private val mutex: Mutex = Mutex()

    internal inline val currentBuffer: Buffer
        get() = if (stageIndex and 1 == 1) backBuffer else frontBuffer

    override suspend fun close() {
        for (element in elements) when (element) {
            is AsyncCloseable -> element.close()
            is AutoCloseable -> element.close()
            else -> {}
        }
        mutex.withLock {
            backBuffer.clear()
            frontBuffer.clear()
        }
    }

    override fun closeAbruptly() {
        for (element in elements) when (element) {
            is AsyncCloseable -> element.closeAbruptly()
            is AutoCloseable -> element.close()
            else -> {}
        }
        backBuffer.clear()
        frontBuffer.clear()
    }

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long = mutex.withLock {
        source.readAtMostTo(currentBuffer, byteCount)
        for (element in elements) element(this)
        val bytesTransformed = currentBuffer.size
        currentBuffer.transferTo(sink)
        stageIndex = 0
        return bytesTransformed
    }
}