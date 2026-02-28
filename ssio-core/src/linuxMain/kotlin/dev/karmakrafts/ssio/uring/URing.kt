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

package dev.karmakrafts.ssio.uring

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import liburing.io_uring
import liburing.io_uring_queue_exit
import liburing.io_uring_queue_init

@OptIn(ExperimentalForeignApi::class)
internal class URing {
    companion object {
        val isAvailable: Boolean by lazy {
            // In order to determine if io_uring is supported, we probe by initializing a ring
            memScoped {
                val ring = alloc<io_uring>()
                val result = io_uring_queue_init(1U, ring.ptr, 0U)
                if (result < 0) {
                    return@memScoped false
                }
                io_uring_queue_exit(ring.ptr)
                true
            }
        }
    }
}