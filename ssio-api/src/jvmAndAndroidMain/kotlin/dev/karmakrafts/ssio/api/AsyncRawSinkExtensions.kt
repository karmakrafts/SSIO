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

@file:JvmName("AsyncRawSinkExtensionsJvm")

package dev.karmakrafts.ssio.api

import kotlinx.io.asByteChannel
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import java.io.OutputStream
import java.nio.channels.WritableByteChannel

/**
 * Returns an [OutputStream] that writes to this [AsyncRawSink] by blocking the current thread.
 *
 * The [AsyncRawSink] is buffered before being converted to an [OutputStream].
 *
 * @return the [OutputStream]
 */
fun AsyncRawSink.asOutputStream(): OutputStream = asBlocking().buffered().asOutputStream()

/**
 * Returns a [WritableByteChannel] that writes to this [AsyncRawSink] by blocking the current thread.
 *
 * The [AsyncRawSink] is buffered before being converted to a [WritableByteChannel].
 *
 * @return the [WritableByteChannel]
 */
fun AsyncRawSink.asByteChannel(): WritableByteChannel = asBlocking().buffered().asByteChannel()