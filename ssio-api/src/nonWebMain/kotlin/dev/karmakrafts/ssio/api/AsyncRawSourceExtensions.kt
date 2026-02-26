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

package dev.karmakrafts.ssio.api

import kotlinx.io.RawSource

/**
 * Wraps this [AsyncRawSource] into a blocking [RawSource].
 *
 * @return a blocking [RawSource] wrapping this [AsyncRawSource]
 */
fun AsyncRawSource.asBlocking(): RawSource = BlockingRawSourceWrapper(this)