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

@file:OptIn(InternalApi::class)

package dev.karmakrafts.ssio

import js.buffer.ArrayBuffer
import js.internal.InternalApi
import js.typedarrays.Int8Array
import js.typedarrays.internal.castOrConvertToByteArray
import js.typedarrays.toInt8Array

internal actual fun ByteArray.asInt8Array(): Int8Array<ArrayBuffer> {
    return toInt8Array()
}

internal actual fun Int8Array<ArrayBuffer>.asByteArray(): ByteArray {
    return castOrConvertToByteArray()
}