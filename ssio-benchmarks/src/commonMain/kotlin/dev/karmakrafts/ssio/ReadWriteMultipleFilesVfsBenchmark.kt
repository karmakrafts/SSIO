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
import dev.karmakrafts.ssio.vfs.AsyncVirtualFileSystem
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.internal.KotlinxBenchmarkRuntimeInternalApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlin.random.Random
import kotlin.time.Clock

@OptIn(KotlinxBenchmarkRuntimeInternalApi::class)
@State(Scope.Benchmark)
open class ReadWriteMultipleFilesVfsBenchmark : AsyncBenchmark<Unit>() {
    companion object {
        private const val FILE_COUNT: Int = 10
        private const val IOPS: Int = 20
        private const val BASE_FILE_NAME: String = "rm_vfs_benchmark_"
        private const val BYTE_COUNT: Int = 8192
    }

    val rand: Random = Random(Clock.System.now().epochSeconds)
    val vfs: AsyncVirtualFileSystem = AsyncVirtualFileSystem()

    override suspend fun run() {
        (0..<FILE_COUNT).map { index ->
            coroutineScope {
                async {
                    vfs.sink(Path("$BASE_FILE_NAME$index.bin")).buffered().use { sink ->
                        for (j in 0..<IOPS) {
                            sink.writeByteArray(rand.nextBytes(BYTE_COUNT))
                        }
                    }
                }
            }
        }.joinAll()
        (0..<FILE_COUNT).map { index ->
            coroutineScope {
                async {
                    vfs.source(Path("$BASE_FILE_NAME$index.bin")).buffered().use { sink ->
                        for (j in 0..<IOPS) {
                            blackHole.consume(sink.readByteArray(BYTE_COUNT))
                        }
                    }
                }
            }
        }.joinAll()
    }
}