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

actual inline fun Short.reverseBytes(): Short {
    val i = toInt() and 0xffff
    val reversed = (i and 0xff00 ushr 8) or (i and 0x00ff shl 8)
    return reversed.toShort()
}

actual inline fun Int.reverseBytes(): Int { // @formatter:off
    return (this and -0x1000000 ushr 24) or
        (this and 0x00ff0000 ushr 8) or
        (this and 0x0000ff00 shl 8) or
        (this and 0x000000ff shl 24)
} // @formatter:on

actual inline fun Long.reverseBytes(): Long { // @formatter:off
    return (this and -0x100000000000000L ushr 56) or
        (this and 0x00ff000000000000L ushr 40) or
        (this and 0x0000ff0000000000L ushr 24) or
        (this and 0x000000ff00000000L ushr 8) or
        (this and 0x00000000ff000000L shl 8) or
        (this and 0x0000000000ff0000L shl 24) or
        (this and 0x000000000000ff00L shl 40) or
        (this and 0x00000000000000ffL shl 56)
} // @formatter:on