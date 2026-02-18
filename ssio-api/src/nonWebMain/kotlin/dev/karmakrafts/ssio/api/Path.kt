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

@file:JvmName("PathImpl")

package dev.karmakrafts.ssio.api

import kotlin.jvm.JvmName
import kotlinx.io.files.Path as KxioPath

actual typealias Path = KxioPath

actual fun Path(path: String): Path = KxioPath(path)

actual fun Path.toKxio(): KxioPath = this
actual fun KxioPath.toSsio(): Path = this