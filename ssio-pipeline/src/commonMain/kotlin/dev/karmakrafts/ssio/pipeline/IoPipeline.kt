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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer

class IoPipeline internal constructor( // @formatter:off
    private val source: AsyncRawSource,
    private val stages: List<IoPipelineStage>
) : AsyncRawSource, AsyncCloseable { // @formatter:on
    private val backBuffer: Buffer = Buffer()
    private val frontBuffer: Buffer = Buffer()
    private val mutex: Mutex = Mutex()

    override suspend fun close() {
        for (stage in stages) stage.close()
        mutex.withLock {
            backBuffer.clear()
            frontBuffer.clear()
        }
    }

    override fun closeAbruptly() {
        for (stage in stages) stage.closeAbruptly()
        backBuffer.clear()
        frontBuffer.clear()
    }

    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long = mutex.withLock {
        source.readAtMostTo(backBuffer, byteCount)
        for (stageIndex in stages.indices) {
            val stage = stages[stageIndex]
            if (stageIndex and 1 == 1) stage(backBuffer, frontBuffer)
            else stage(frontBuffer, backBuffer)
        }
        val sourceBuffer = if (stages.size and 1 == 1) frontBuffer
        else backBuffer
        val bytesTransformed = sourceBuffer.size
        sourceBuffer.transferTo(sink)
        return bytesTransformed
    }
}