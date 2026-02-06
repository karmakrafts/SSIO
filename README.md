# 🔀 SSIO

[![](https://git.karmakrafts.dev/kk/ssio/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/ssio/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Fssio%2Fssio-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/ssio/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Fssio%2Fssio-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/ssio/-/packages)
[![](https://img.shields.io/badge/2.3.20--Beta2-blue?logo=kotlin&label=kotlin)](https://kotlinlang.org/)
[![](https://img.shields.io/badge/documentation-black?logo=kotlin)](https://docs.karmakrafts.dev/ssio-core)

**S**treaming **S**uspend **I**nput **O**utput library for Kotlin Multiplatform.  
This is an async IO extension for [kotlinx.io](https://github.com/Kotlin/kotlinx-io) based on [the async API proposal](https://github.com/Kotlin/kotlinx-io/issues/163) which  
was never implemented.

### Features

- Supports all Kotlin Multiplatform targets
- `AsyncReadOnlyFileSystem` interface
- `AsyncFileSystem` interface
- `AsyncCloseable` interface
- `AsyncRawSource` interface
- `AsyncRawSink` interface
- `AsyncSource` wrapper for buffering raw async sources
- `AwaitPredicate` interface for await conditions like `exhausted` and `available`
- Utility functions like `discardingAsyncSink` and `buffered` extensions

### Without SSIO

<img src="docs/blocking.gif" alt="Metaphoric meme about the german rapper SSIO to illustrate blocking IO" width="512">

Busy waiting, stalling and wasted time when performing potentially long-blocking IO tasks.

### With SSIO

<img src="docs/async.gif" alt="Metaphoric meme about the german rapper SSIO to illustrate async IO" width="512">

Concurrent execution, no stalling and all time used to perform actual work while handling IO.

## How to use it

First, add the official Maven Central repository to your settings.gradle.kts:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

Then add a dependency on the library in your root buildscript:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("dev.karmakrafts.ssio:ssio-core:<version>")
            }
        }
    }
}
```

Or, if you are only using Kotlin/JVM, add it to your top-level dependencies block instead.