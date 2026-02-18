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
import dev.karmakrafts.ssio.api.AwaitPredicate
import dev.karmakrafts.ssio.api.ExperimentalSsioApi

@ExperimentalSsioApi
@Suppress("NOTHING_TO_INLINE")
@IoPipelineDsl
class IoPipelineBuilder @PublishedApi internal constructor( // @formatter:off
    private val source: AsyncRawSource,
    private val elements: MutableList<IoPipelineElement> = ArrayList()
) { // @formatter:on
    fun <E : IoPipelineElement> element(element: E): E {
        require(element !in elements) { "Element $element is already registered in IoPipeline" }
        elements += element
        return element
    }

    fun <E : IoPipelineElement> elementBefore(before: IoPipelineElement, element: E): E {
        require(before in elements) { "Cannot insert stage into IoPipeline before unknown stage" }
        require(element !in elements) { "Stage is already registered in IoPipeline" }
        val index = elements.indexOf(before) - 1
        check(index >= 0) { "Cannot insert stage into IoPipeline before unknown stage" }
        elements.add(index, element)
        return element
    }

    fun <E : IoPipelineElement> elementAfter(after: IoPipelineElement, element: E): E {
        require(after in elements) { "Cannot insert stage into IoPipeline before unknown stage" }
        require(element !in elements) { "Stage is already registered in IoPipeline" }
        val index = elements.indexOf(after) + 1
        check(index in elements.indices) { "Cannot insert stage into IoPipeline before unknown stage" }
        elements.add(index, element)
        return element
    }

    // @formatter:off
    inline fun stage(stage: IoPipelineStage): IoPipelineStage = element(stage)
    inline fun stageBefore(before: IoPipelineElement, stage: IoPipelineStage): IoPipelineStage = elementBefore(before, stage)
    inline fun stageAfter(after: IoPipelineElement, stage: IoPipelineStage): IoPipelineStage = elementAfter(after, stage)

    inline fun barrier(predicate: AwaitPredicate = AwaitPredicate.exhausted()): IoPipelineBarrier = element(IoPipelineBarrier(predicate))
    inline fun barrierBefore(before: IoPipelineElement, predicate: AwaitPredicate = AwaitPredicate.exhausted()): IoPipelineBarrier = elementBefore(before, IoPipelineBarrier(predicate))
    inline fun barrierAfter(after: IoPipelineElement, predicate: AwaitPredicate = AwaitPredicate.exhausted()): IoPipelineBarrier = elementAfter(after, IoPipelineBarrier(predicate))

    inline fun limit(byteCount: Long): IoPipelineStage.Limit = element(IoPipelineStage.Limit(byteCount))
    inline fun limitBefore(before: IoPipelineElement, byteCount: Long): IoPipelineStage.Limit = elementBefore(before, IoPipelineStage.Limit(byteCount))
    inline fun limitAfter(after: IoPipelineElement, byteCount: Long): IoPipelineStage.Limit = elementAfter(after, IoPipelineStage.Limit(byteCount))

    inline fun drop(byteCount: Long): IoPipelineStage.Drop = element(IoPipelineStage.Drop(byteCount))
    inline fun dropBefore(before: IoPipelineElement, byteCount: Long): IoPipelineStage.Drop = elementBefore(before, IoPipelineStage.Drop(byteCount))
    inline fun dropAfter(after: IoPipelineElement, byteCount: Long): IoPipelineStage.Drop = elementAfter(after, IoPipelineStage.Drop(byteCount))

    inline fun slice(start: Long, end: Long): IoPipelineStage.Slice = element(IoPipelineStage.Slice(start, end))
    inline fun sliceBefore(before: IoPipelineElement, start: Long, end: Long): IoPipelineStage.Slice = elementBefore(before, IoPipelineStage.Slice(start, end))
    inline fun sliceAfter(after: IoPipelineElement, start: Long, end: Long): IoPipelineStage.Slice = elementAfter(after, IoPipelineStage.Slice(start, end))
    // @formatter:on

    @PublishedApi
    internal fun build(): IoPipeline = IoPipeline(source, elements)
}

@ExperimentalSsioApi
typealias IoPipelineSpec = IoPipelineBuilder.() -> Unit

@ExperimentalSsioApi
inline fun AsyncRawSource.withPipeline(spec: IoPipelineSpec): AsyncRawSource {
    return IoPipelineBuilder(this).apply(spec).build()
}