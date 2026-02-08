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

import dev.karmakrafts.ssio.files.Path
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.internal.KotlinxBenchmarkRuntimeInternalApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.io.files.Path as KxioPath

@OptIn(KotlinxBenchmarkRuntimeInternalApi::class)
@State(Scope.Benchmark)
open class ReadMultipleFilesAsyncBenchmark : AsyncBenchmark<Unit>() {
    companion object {
        private const val FILE_COUNT: Int = 10
        private const val BASE_FILE_NAME: String = "rm_async_benchmark_"
        private const val BYTE_COUNT: Int = 8192
    }

    @Setup
    fun setup() {
        val rand = Random(Clock.System.now().epochSeconds)
        for (i in 0..<FILE_COUNT) {
            SystemFileSystem.sink(KxioPath("$BASE_FILE_NAME$i.bin")).buffered().use { sink ->
                for (j in 0..<20) {
                    sink.write(rand.nextBytes(BYTE_COUNT))
                }
            }
        }
    }

    override suspend fun run() {
        (0..<FILE_COUNT).map { index ->
            coroutineScope {
                async {
                    AsyncSystemFileSystem.source(Path("$BASE_FILE_NAME$index.bin")).buffered().use { sink ->
                        for (j in 0..<20) {
                            blackHole.consume(sink.readByteArray(BYTE_COUNT))
                        }
                    }
                }
            }
        }.joinAll()
    }
}