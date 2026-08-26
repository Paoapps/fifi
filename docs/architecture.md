# FiFi Architecture

## Module diagram

```mermaid
flowchart TB
    subgraph apps [Consumer apps]
        Mobile[KMP mobile app]
        Server[Ktor server]
    end

    subgraph fifi [FiFi published modules]
        FC[fifi-common]
        FFw[fifi-framework]
        FA[fifi-auth]
    end

    BC[blocked-cache]

    Server --> FC
    Mobile --> FFw
    Mobile --> FC
    Mobile --> FA
    FFw --> FC
    FFw --> BC
    FA --> FFw
```

## Data flow

```mermaid
sequenceDiagram
    participant UI as Platform UI
    participant VM as AbstractViewModel
    participant Model as Model / ModelHelper
    participant Cache as blocked-cache
    participant API as ClientApi / Ktor

    UI->>VM: emitEvent
    VM->>Model: createRefreshableFetchFlow
    Model->>Cache: fetch / read cache
    Cache->>API: network when needed
    API-->>Cache: response
    Cache-->>VM: CacheResult
    VM-->>UI: Output Flow
    VM-->>UI: Action Flow
```

## Key types

| Package | Types | Role |
|---------|-------|------|
| `viewmodel` | `AbstractViewModel`, `AbstractEvent`, `VoidEvent` | MVVM core |
| `utils` | `ActionHandler`, `Emitter` | Event → action routing |
| `utils.flow` | `FlowAdapter`, `wrap()` | Cross-platform Flow bridging |
| `model` | `Model`, `ModelImpl`, `ModelHelper`, `DataContainer` | Data layer + cache |
| `loading` | `Loadable`, `asLoadable()` | CacheResult → UI-agnostic state |
| `di` | `AppDefinition`, `initKoinApp`, `PersistentDataRegistry` | Bootstrap |
| `api` | `ClientApi`, `ClientApiImpl`, `ApiFactory` | HTTP layer |
| `ui.component` | `TextDefinition`, `ConfirmationDialogDefinition` | Minimal shared UI contracts |
| `auth` | `AuthAppDefinition`, `AuthApiHelper`, `TokenStore` | Optional auth (fifi-auth) |

## Platform ViewModel lifecycle

| Platform | Scope | Cleanup |
|----------|-------|---------|
| Android | `androidx.lifecycle.ViewModel` + `viewModelScope` | Automatic |
| iOS / JVM | `AppMainScope()` | Manual `clear()` required |

## Persistence

- **Android:** `AndroidApp.setupAppModel()` debounces JSON persistence of registered `DataContainer`s per environment.
- **iOS:** Not provided — apps implement disk restore (e.g. a Swift `DataStore` that serializes `CDataContainer.json`).

## Sample app

The coffee browse sample (`sample/shared`) demonstrates:

- `AppDefinition` without auth
- `ModelHelper` + blocked-cache
- `HomeViewModel` with `createRefreshableFetchFlow`
- Android Compose via `ViewModelComposable`
