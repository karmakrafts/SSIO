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

@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import dev.karmakrafts.conventions.kotlin.defaultCompilerOptions
import dev.karmakrafts.conventions.kotlin.withJvm
import dev.karmakrafts.conventions.kotlin.withLinux
import dev.karmakrafts.conventions.kotlin.withMacos
import dev.karmakrafts.conventions.kotlin.withMingw
import dev.karmakrafts.conventions.kotlin.withNodeJs
import dev.karmakrafts.conventions.kotlin.withWeb
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)
}

kotlin {
    defaultCompilerOptions()
    withJvm()
    withLinux()
    withMingw()
    withMacos()
    withWeb {
        withNodeJs()
    }
    applyDefaultHierarchyTemplate {
        common {
            group("native") {
                withLinux()
                withMacos()
                withMingw()
            }
            group("nonWeb") {
                withJvm()
                group("native")
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.ssioCore)
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
        register("linuxX64")
        register("linuxArm64")
        register("mingwX64")
        register("macosArm64")
        register("js")
        register("wasmJs")
    }
    configurations {
        named("main") {
            warmups = 8
            iterations = 10
            iterationTime = 2
            iterationTimeUnit = "s"
        }
    }
}