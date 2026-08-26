# Changelog

All notable changes to FiFi are documented here.

## [0.0.45] - 2026-08-25

### Added

- **Documentation:** Rewritten README, getting started, architecture, auth, iOS integration, compatibility, and upgrade guides
- **`Loadable`:** UI-agnostic `CacheResult` wrapper in `com.paoapps.fifi.loading`
- **Test utilities:** `testOutput()` and `testActions()` on `AbstractViewModel`
- **Unit tests:** `ActionHandlerTest`, `LoadableTest`
- **Android:** `ViewModelComposable` in fifi-framework for Compose apps
- **iOS sample:** SwiftUI reference app under `sample/iosApp`
- **Skills:** `skills/fifi-screen` and `skills/fifi-component` templates for scaffolding

### Changed

- **Android persistence:** Replaced `GlobalScope` with dedicated scope in `AndroidApp`
- **AppMainScope:** Logs uncaught coroutine errors via FiFi logger
- **README:** Removed stale `initKoinShared` API; documents current `AppDefinition` bootstrap

### Removed

- Dead `multipartUpload` iOS stub (FiftyFifty leakage)
- Commented `Link` event type and unused `links` flow in `ActionHandler`
- Commented polling helper stub in `FlowUtils`

## [0.0.44] - prior release

- Keychain migration improvements (fifi-auth)
- Ktor 3.5.x, JVM 17 target

[0.0.45]: https://github.com/Paoapps/fifi/compare/v0.0.44...v0.0.45
