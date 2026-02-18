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
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.internal.KotlinxBenchmarkRuntimeInternalApi
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.io.files.Path as KxioPath

@OptIn(KotlinxBenchmarkRuntimeInternalApi::class)
@State(Scope.Benchmark)
open class ReadWriteMultipleFilesBenchmark {
    companion object {
        private const val FILE_COUNT: Int = 10
        private const val IOPS: Int = 20
        private const val BASE_FILE_NAME: String = "rm_benchmark_"
        private const val BYTE_COUNT: Int = 8192
    }

    val rand: Random = Random(Clock.System.now().epochSeconds)

    @Benchmark
    fun invoke(blackHole: Blackhole) {
        for (i in 0..<FILE_COUNT) {
            SystemFileSystem.sink(KxioPath("$BASE_FILE_NAME$i.bin")).buffered().use { sink ->
                for (j in 0..<IOPS) {
                    sink.write(rand.nextBytes(BYTE_COUNT))
                }
            }
        }
        for (i in 0..<FILE_COUNT) {
            SystemFileSystem.source(KxioPath("$BASE_FILE_NAME$i.bin")).buffered().use { source ->
                for (j in 0..<IOPS) {
                    blackHole.consume(source.readByteArray(BYTE_COUNT))
                }
            }
        }
    }
}