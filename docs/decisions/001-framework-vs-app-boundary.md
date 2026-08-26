# ADR 001: Framework vs App Boundary

## Status

Accepted

## Context

FiFi is consumed by production apps that each built large design systems (Definitions, Tokens, Providers) on top of minimal FiFi UI primitives. App code has leaked into FiFi before (e.g. FiftyFifty-specific stubs). We need a clear boundary for maintainability and adoption.

## Decision

### Belongs in FiFi

| Area | Examples |
|------|----------|
| MVVM core | `AbstractViewModel`, `ActionHandler`, `FlowAdapter` |
| Data layer | `Model`, `ModelHelper`, `DataContainer`, blocked-cache integration |
| Bootstrap | `AppDefinition`, `initKoinApp`, `PersistentDataRegistry` |
| API client base | `ClientApi`, `ClientApiImpl`, `ApiHelper` |
| Auth (optional module) | `fifi-auth`, token refresh, encrypted storage abstractions |
| Shared primitives | `TextDefinition`, `ConfirmationDialogDefinition`, color utils |
| Cross-app glue | `Loadable`, ViewModel test helpers (`testOutput`, `testActions`) |
| Platform lifecycle docs | iOS `clear()`, Android persistence helper |

### Belongs in the app

| Area | Examples |
|------|----------|
| Navigation | Compose Navigation, SwiftUI routes, sealed `Action` handlers |
| Design system | `ColorToken`, `FontToken`, `*Definition.kt` catalogs, style providers |
| Icons | `SealedImage`, `SystemImage` enums |
| Brand UI | All `*Component.kt` / `*Component.swift` implementations |
| Analytics / toasts | `AbstractAppViewModel` subclasses, snackbar flows |
| Auth product logic | Custom auth stacks or app-specific JWT claim shapes |
| Global action product types | App-defined `GlobalAction` sealed classes |

### Never in FiFi

- App-specific API endpoints or domain models
- Product copy, localization resources (use app-owned moko-resources)
- Incomplete stubs copied from a consumer (`multipartUpload`-style leakage)

## Consequences

- FiFi stays a **framework**, not an app SDK.
- Docs and skills describe the **Definition pattern** as an app convention, not a FiFi export.
- New shared glue (Loadable, test utils) goes into FiFi when **two apps duplicate it** and it has **no UI/brand dependency**.
- Design system convergence across apps is optional and app-driven — not a FiFi release blocker.
