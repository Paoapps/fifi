# Compatibility Policy

FiFi is currently at **0.x** semver. Breaking API changes may occur on minor versions until **0.1.0**.

## Versioning rules (0.x)

| Change | Version bump |
|--------|--------------|
| Bug fix, docs, internal cleanup | Patch (`0.0.x`) |
| New API, deprecations | Minor (`0.0.x` when patch exceeds 99, or coordinated minor) |
| Breaking public API | Minor with changelog entry (0.x allows breaks on minor) |

## Public API surface

Stable (intended for apps):

- `AbstractViewModel`, events, actions pattern
- `AppDefinition` / `initKoinApp`
- `AuthAppDefinition` / auth `initKoinApp`
- `Model`, `ModelHelper`, `Loadable`
- `viewModel` Koin DSL
- `testOutput`, `testActions`

Internal / may change:

- Koin qualifier constants marked `internal`
- Ktor logging plugin internals
- Undocumented helpers reached around by convention

## Consumer alignment

Maintain **one supported line** across internal apps. As of 0.0.45, treat FiftyFifty as the canary consumer and keep other apps within one minor release of that pin (see [upgrading.md](upgrading.md)).

## Binary compatibility

Binary compatibility validation (`api` dump / ABI validator) is planned for **0.1.0** once docs, tests, and dual-platform sample are stable.

## Releases

- Published to Maven Central on GitHub **release** (see `.github/workflows/publish.yml`)
- Every release updates [CHANGELOG.md](../CHANGELOG.md)

## blocked-cache

FiFi depends on blocked-cache but versions independently. Pin both explicitly in consumer apps and upgrade together when cache API changes.
