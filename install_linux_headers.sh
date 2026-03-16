#!/bin/bash

#
# Copyright 2026 Karma Krafts
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -euo pipefail

# Determine correct target architecture
if [ "$TARGETARCH" = "arm64" ]; then
	ARCH="arm64"
elif [ "$TARGETARCH" = "amd64" ]; then
	ARCH="x86_64"
fi

echo "Downloading Linux sources.."
cd /tmp/
git clone --depth 1 https://git.launchpad.net/ubuntu/+source/linux >/dev/null
cd linux/
echo "Checking out tag $1.."
git fetch --depth 1 origin tag $1 >/dev/null
git checkout $1 >/dev/null

echo "Building generic Linux headers.."
NUM_THREADS=$(grep -c ^processor /proc/cpuinfo)
echo "Using $NUM_THREADS threads for building"
make defconfig
make ARCH=$ARCH O=build headers_install INSTALL_HDR_PATH=/usr/src/linux-headers -- -j $NUM_THREADS