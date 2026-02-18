# 🔀 SSIO

[![](https://git.karmakrafts.dev/kk/ssio/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/ssio/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Fssio%2Fssio-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/ssio/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Fssio%2Fssio-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/ssio/-/packages)
[![](https://img.shields.io/badge/2.3.20--Beta2-blue?logo=kotlin&label=kotlin)](https://kotlinlang.org/)
[![](https://img.shields.io/badge/documentation-black?logo=kotlin)](https://docs.karmakrafts.dev/ssio-core)

**S**treaming **S**uspend **I**nput **O**utput library for Kotlin Multiplatform.  
This is an async IO extension for [kotlinx.io](https://github.com/Kotlin/kotlinx-io) based on [the async API proposal](https://github.com/Kotlin/kotlinx-io/issues/163).

### Features

- Supports all Kotlin Multiplatform targets
- `AsyncReadOnlyFileSystem` interface
- `AsyncFileSystem` interface
- `AsyncCloseable` interface
- `AsyncRawSource` interface
- `AsyncRawSink` interface
- `AsyncSource` wrapper for buffering raw async sources
- `AwaitPredicate` interface for await conditions like `exhausted` and `available`
- `DiscardingAsyncSink` object for voiding data
- `RandomAsyncSource` object for sourcing an endless pseudo-random byte stream
- `AsyncSystemFileSystem` global file system instance via the `ssio-core` module
- `AsyncVirtualFileSystem` class via the `ssio-vfs` module
- `AsyncNoopFileSystem` object via the `ssio-noop` module

### Platforms

The following matrix illustrates what implementations are used for which target & runtime combination  
in the `ssio-core` implementation:

| Target  | Runtime       | Implementation |
|---------|---------------|----------------|
| JVM     | JVM           | NIO            |
| Android | ART           | NIO            |
| Native  | Kotlin/Native | CIO            |
| JS      | Browser       | OPFS           |
| JS      | NodeJS        | fs.promises    |
| WASM    | Browser       | OPFS           |
| WASM    | NodeJS        | fs.promises    |

### Why should I use this over kotlinx.io or java.nio?

The answer is simple: you shouldn't if you don't need a suspend IO API available in your common source set.  
If you do, this library is probably what you're looking for.

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