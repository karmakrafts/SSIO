## [Unreleased]

### Added

- `AsyncRawSource.asInputStream` for JVM & Android
- `AsyncRawSource.asByteChannel` for JVM & Android
- `AsyncRawSink.asOutputStream` for JVM & Android
- `AsyncRawSink.asByteChannel` for JVM & Android
- `AsyncRawSource.asBlocking` for all targets except web
- `AsyncRawSink.asBlocking` for all targets except web

### Fixed

- `RawSource.asAsync` not chunking work onto platform IO dispatcher
- `RawSink.asAsync` not chunking work onto platform IO dispatcher
- Default internal buffer size being 8192 bytes in some places instead of 4096 bytes

## [1.2.0]

### Changed

- Updated to Karma Conventions 1.12.1
- Updated to Kotlin 2.3.20-RC

## [1.1.0]

### Added

- Added automatic changelog & releases
- Added NoOp module
- Added VFS module
- Added pipeline module
- Added `ExperimentalSsioApi` annotation for incubating APIs
- Added RandomAsyncSource
- Added DiscardingAsyncSink

### Changed

- Updated to Karma Conventions 1.11.4
- Split IO APIs into separate module