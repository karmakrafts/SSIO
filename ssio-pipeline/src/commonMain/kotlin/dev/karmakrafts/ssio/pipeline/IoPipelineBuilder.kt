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

@IoPipelineDsl
class IoPipelineBuilder @PublishedApi internal constructor( // @formatter:off
    private val source: AsyncRawSource,
    private val stages: MutableList<IoPipelineStage> = ArrayList()
) { // @formatter:on
    fun stage(stage: IoPipelineStage): IoPipelineStage {
        require(stage !in stages) { "Stage is already registered in IoPipeline" }
        stages += stage
        return stage
    }

    fun stageBefore(before: IoPipelineStage, stage: IoPipelineStage): IoPipelineStage {
        require(before in stages) { "Cannot insert stage into IoPipeline before unknown stage" }
        require(stage !in stages) { "Stage is already registered in IoPipeline" }
        val index = stages.indexOf(before) - 1
        check(index >= 0) { "Cannot insert stage into IoPipeline before unknown stage" }
        stages.add(index, stage)
        return stage
    }

    fun stageAfter(after: IoPipelineStage, stage: IoPipelineStage): IoPipelineStage {
        require(after in stages) { "Cannot insert stage into IoPipeline before unknown stage" }
        require(stage !in stages) { "Stage is already registered in IoPipeline" }
        val index = stages.indexOf(after) + 1
        check(index in stages.indices) { "Cannot insert stage into IoPipeline before unknown stage" }
        stages.add(index, stage)
        return stage
    }

    @PublishedApi
    internal fun build(): IoPipeline = IoPipeline(source, stages)
}

typealias IoPipelineSpec = IoPipelineBuilder.() -> Unit

inline fun AsyncRawSource.withPipeline(spec: IoPipelineSpec): AsyncRawSource {
    return IoPipelineBuilder(this).apply(spec).build()
}