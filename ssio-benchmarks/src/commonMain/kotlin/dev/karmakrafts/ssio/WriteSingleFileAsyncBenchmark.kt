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
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlin.random.Random
import kotlin.time.Clock

@State(Scope.Benchmark)
open class WriteSingleFileAsyncBenchmark : AsyncBenchmark<Unit>() {
    companion object {
        private const val BYTE_COUNT: Int = 8192
    }

    private val rand: Random = Random(Clock.System.now().epochSeconds)

    override suspend fun run() {
        val sink = AsyncSystemFileSystem.sink(Path("benchmark_async.bin")).buffered()
        for (i in 0..<10) {
            sink.writeByteArray(rand.nextBytes(BYTE_COUNT))
        }
        sink.close()
    }
}