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

@file:Suppress("NOTHING_TO_INLINE")

package dev.karmakrafts.ssio

import platform.builtin.builtin_bswap16
import platform.builtin.builtin_bswap32
import platform.builtin.builtin_bswap64

actual inline fun Short.reverseBytes(): Short = builtin_bswap16(this)

actual inline fun Int.reverseBytes(): Int = builtin_bswap32(this)

actual inline fun Long.reverseBytes(): Long = builtin_bswap64(this)