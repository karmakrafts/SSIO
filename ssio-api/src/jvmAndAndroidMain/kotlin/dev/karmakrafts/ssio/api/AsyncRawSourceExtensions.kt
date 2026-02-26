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

@file:JvmName("AsyncRawSourceExtensionsJvm")

package dev.karmakrafts.ssio.api

import kotlinx.io.asByteChannel
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import java.io.InputStream
import java.nio.channels.ReadableByteChannel

/**
 * Returns an [InputStream] that reads from this [AsyncRawSource] by blocking the current thread.
 *
 * The [AsyncRawSource] is buffered before being converted to an [InputStream].
 *
 * @return the [InputStream]
 */
fun AsyncRawSource.asInputStream(): InputStream = asBlocking().buffered().asInputStream()

/**
 * Returns a [ReadableByteChannel] that reads from this [AsyncRawSource] by blocking the current thread.
 *
 * The [AsyncRawSource] is buffered before being converted to a [ReadableByteChannel].
 *
 * @return the [ReadableByteChannel]
 */
fun AsyncRawSource.asByteChannel(): ReadableByteChannel = asBlocking().buffered().asByteChannel()