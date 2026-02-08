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

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.random.Random
import kotlin.time.Clock

@State(Scope.Benchmark)
open class WriteMultipleFilesBenchmark {
    companion object {
        private const val BYTE_COUNT: Int = 4096
    }

    private val rand: Random = Random(Clock.System.now().epochSeconds)

    @Benchmark
    fun invoke() {
        for (i in 0..<10) {
            SystemFileSystem.sink(Path("mf_benchmark_$i.bin")).buffered().use { sink ->
                for(j in 0..<20) {
                    sink.write(rand.nextBytes(BYTE_COUNT))
                }
            }
        }
    }
}