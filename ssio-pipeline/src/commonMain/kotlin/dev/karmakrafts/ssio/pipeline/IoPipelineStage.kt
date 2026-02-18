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
import dev.karmakrafts.ssio.api.ExperimentalSsioApi
import kotlinx.io.Buffer

@ExperimentalSsioApi
fun interface IoPipelineStage : IoPipelineElement, AsyncCloseable {
    suspend operator fun invoke(input: Buffer, output: Buffer)
    override suspend fun close() = Unit
    override fun closeAbruptly() = Unit

    override suspend fun invoke(pipeline: IoPipeline) = with<_, Unit>(pipeline) {
        if (stageIndex++ and 1 == 1) {
            frontBuffer.clear()
            this@IoPipelineStage(backBuffer, frontBuffer)
        }
        else {
            backBuffer.clear()
            this@IoPipelineStage(frontBuffer, backBuffer)
        }
    }

    data class Limit(val byteCount: Long) : IoPipelineStage {
        override suspend fun invoke(input: Buffer, output: Buffer) {
            output.write(input, byteCount)
        }
    }

    data class Drop(val byteCount: Long) : IoPipelineStage {
        override suspend fun invoke(input: Buffer, output: Buffer) {
            input.skip(byteCount)
            input.transferTo(output)
        }
    }

    data class Slice(val start: Long, val end: Long) : IoPipelineStage {
        override suspend fun invoke(input: Buffer, output: Buffer) {
            input.skip(start)
            output.write(input, end - start)
        }
    }
}