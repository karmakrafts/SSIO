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

import dev.karmakrafts.ssio.api.Path
import dev.karmakrafts.ssio.api.buffered
import dev.karmakrafts.ssio.api.use
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlin.random.Random
import kotlin.time.Clock

@State(Scope.Benchmark)
open class WriteMultipleFilesAsyncBenchmark : AsyncBenchmark<Unit>() {
    companion object {
        private const val BYTE_COUNT: Int = 4096
    }

    override suspend fun run() {
        val rand = Random(Clock.System.now().epochSeconds)
        (0..<10).map { i ->
            coroutineScope {
                async {
                    AsyncSystemFileSystem.sink(Path("mf_benchmark_async_$i.bin")).buffered().use { sink ->
                        for (j in 0..<20) {
                            sink.writeByteArray(rand.nextBytes(BYTE_COUNT))
                        }
                    }
                }
            }
        }.joinAll()
    }
}