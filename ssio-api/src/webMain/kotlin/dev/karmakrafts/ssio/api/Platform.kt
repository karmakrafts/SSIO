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

// TODO: we consider FreeBSD Linux right now for the sake of simplicity, change this
internal actual val platform: Platform = if (isNode) when (getNodePlatform()) {
    "darwin" -> Platform.MACOS
    "linux", "freebsd", "openbsd" -> Platform.LINUX
    "win32", "cygwin" -> Platform.WINDOWS
    else -> Platform.UNKNOWN
}
else Platform.BROWSER