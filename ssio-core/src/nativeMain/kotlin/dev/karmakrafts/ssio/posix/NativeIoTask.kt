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

package dev.karmakrafts.ssio.posix

import kotlinx.coroutines.CompletableDeferred

internal sealed interface NativeIoTask {
    data class Read( // @formatter:off
        val action: (Int) -> Long,
        val result: CompletableDeferred<Long> = CompletableDeferred()
    ) : NativeIoTask // @formatter:on

    data class Write( // @formatter:off
        val action: (Int) -> Unit,
        val result: CompletableDeferred<Unit> = CompletableDeferred()
    ) : NativeIoTask // @formatter:on

    data class Flush( // @formatter:off
        val result: CompletableDeferred<Unit> = CompletableDeferred()
    ) : NativeIoTask // @formatter:on
}