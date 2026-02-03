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

import dev.karmakrafts.conventions.asAAR
import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.defaultDokkaConfig
import dev.karmakrafts.conventions.kotlin.defaultCompilerOptions
import dev.karmakrafts.conventions.kotlin.withAndroidLibrary
import dev.karmakrafts.conventions.kotlin.withBrowser
import dev.karmakrafts.conventions.kotlin.withJvm
import dev.karmakrafts.conventions.kotlin.withNative
import dev.karmakrafts.conventions.kotlin.withNodeJs
import dev.karmakrafts.conventions.kotlin.withWeb
import dev.karmakrafts.conventions.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    signing
    `maven-publish`
}

configureJava(libs.versions.java)
defaultDokkaConfig()

kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withJvm()
    withAndroidLibrary("$group.core")
    withNative()
    withWeb {
        withBrowser {
            useEsModules()
        }
        withNodeJs()
    }
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidLibrary()
            }
            group("ios") { withIos() }
            group("tvos") { withTvos() }
            group("watchos") { withWatchos() }
            group("native") {
                group("posix") { // All non-Windows OSs can use mmap no problem
                    withAndroidNative()
                    withLinux()
                    withMacos()
                    group("ios")
                    group("tvos")
                    group("watchos")
                }
            }
            group("nonWeb") {
                withJvm()
                withAndroidNative()
                withLinux()
                withMacos()
                group("ios")
                group("tvos")
                group("watchos")
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.io.bytestring)
                api(libs.kotlinx.io.core)
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        webMain {
            dependencies {
                api(libs.kotlin.wrappers.browser)
            }
        }
        androidMain {
            dependencies {
                api(libs.jna.asAAR())
            }
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
    }
    withType<JavaExec> {
        jvmArgs("--enable-preview")
    }
}

publishing {
    setProjectInfo(
        name = "SSIO Core",
        description = "Async IO extension for kotlinx.io",
        url = "https://git.karmakrafts.dev/kk/SSIO"
    )
}