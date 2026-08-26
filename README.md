# FiFi: Kotlin Multiplatform Mobile Framework

FiFi is a Kotlin Multiplatform (KMP) framework for sharing mobile app logic between iOS and Android: ViewModels, data models, caching, API clients, and optional token auth. UI and navigation stay in platform code (Compose / SwiftUI).

**Current version:** `0.0.45` (see [CHANGELOG.md](CHANGELOG.md))

## Modules

| Module | Maven artifact | Use when |
|--------|----------------|----------|
| **fifi-common** | `com.paoapps.fifi:fifi-common` | Shared types with a Ktor server (amounts, tokens, JSON helpers) |
| **fifi-framework** | `com.paoapps.fifi:fifi-framework` | Any KMP mobile app (ViewModels, Model, caching, DI bootstrap) |
| **fifi-auth** | `com.paoapps.fifi:fifi-auth` | Apps with token-based authentication |

**Peer dependency:** [blocked-cache](https://github.com/Paoapps/blocked-cache) (`com.paoapps.blockedcache:blocked-cache`) — used by `ModelHelper` for fetch/cache flows.

```kotlin
// build.gradle.kts (shared module)
implementation("com.paoapps.fifi:fifi-framework:0.0.45")
implementation("com.paoapps.blockedcache:blocked-cache:0.0.10")
```

## Quick start

1. Implement [`AppDefinition`](fifi-framework/src/commonMain/kotlin/com/paoapps/fifi/di/Koin.kt) — environment factory, API factory, Model, Koin modules.
2. Call `initKoinApp(appDefinition)` from Android/iOS entry points (Android: `com.paoapps.fifi.koin.initKoinApp(context, …)`).
3. Create ViewModels extending `AbstractViewModel<Output, Event, Action>`.
4. Bind platform UI to `output`, `action`, and `emitEvent()`.

See [docs/getting-started.md](docs/getting-started.md) for a full checklist. The [sample app](sample/) demonstrates a non-auth setup.

## Core concepts

### ViewModel (Output / Event / Action)

- **Output** — UI state (`Flow` / `FlowAdapter`)
- **Event** — user input via `emitEvent()`
- **Action** — one-shot navigation or side effects

Helpers: `viewModelOutput()`, `createRefreshableFetchFlow()`, `ActionHandler`.

### Model + caching

Models fetch and cache data via `ModelHelper` and **blocked-cache**. ViewModels request data; Models decide cache vs network.

### Loadable

`com.paoapps.fifi.loading.Loadable` maps `CacheResult` to a UI-agnostic loading/error/data wrapper. Apps map it to their own error components.

### Testing

```kotlin
import com.paoapps.fifi.viewmodel.testOutput
import com.paoapps.fifi.viewmodel.testActions

viewModel.testOutput { /* assert on Output */ }
viewModel.testActions { emit -> emit(MyEvent.Tap) }
```

## Auth apps

Use `fifi-auth`: implement `AuthAppDefinition`, `TokenDecoder`, and call `com.paoapps.fifi.auth.di.initKoinApp`. See [docs/auth-setup.md](docs/auth-setup.md).

## iOS notes

- ViewModels use a custom scope; call `viewModel.clear()` when the screen is dismissed (see [docs/ios-integration.md](docs/ios-integration.md)).
- Disk persistence for `DataContainer` is Android-only in-framework; iOS apps provide their own storage bridge.

## Documentation

| Doc | Description |
|-----|-------------|
| [Getting started](docs/getting-started.md) | Bootstrap checklist |
| [Architecture](docs/architecture.md) | Module and data-flow overview |
| [Auth setup](docs/auth-setup.md) | fifi-auth integration |
| [iOS integration](docs/ios-integration.md) | SwiftUI bridge, lifecycle, persistence |
| [Framework vs app](docs/decisions/001-framework-vs-app-boundary.md) | What belongs in FiFi vs your app |
| [Compatibility](docs/compatibility.md) | Versioning policy |
| [Upgrading](docs/upgrading.md) | Consumer upgrade notes |

## Sample app

- **Android:** `sample/android` — Compose UI wired to shared ViewModels
- **iOS:** `sample/iosApp` — SwiftUI reference (see README in that folder)

Run Android sample: `./gradlew :sample:android:installDebug`

## Publishing

Released to Maven Central on GitHub release. Version in `gradle.properties`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
