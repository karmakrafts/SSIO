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

import dev.karmakrafts.ssio.api.ExperimentalSsioApi

@ExperimentalSsioApi
fun IoPipeline.limit(byteCount: Long): IoPipeline = transform { input, output ->
    output.write(input, byteCount)
}

@ExperimentalSsioApi
fun IoPipeline.drop(byteCount: Long): IoPipeline = transform { input, output ->
    input.skip(byteCount)
    input.transferTo(output)
}

@ExperimentalSsioApi
fun IoPipeline.slice(start: Long, end: Long): IoPipeline = transform { input, output ->
    input.skip(start)
    output.write(input, end - start)
}

@ExperimentalSsioApi
fun IoPipeline.slice(range: LongRange): IoPipeline = slice(range.first, range.last)