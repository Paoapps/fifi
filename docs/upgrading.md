# Upgrading FiFi

## 0.0.44 → 0.0.45

### Dependency bump

```toml
# gradle/libs.versions.toml
fifi = "0.0.45"
blockedcache = "0.0.10"  # verify current; keep aligned with FiFi sample
```

### Expected changes

1. **Loadable (optional migration)** — FiFi now ships UI-agnostic `com.paoapps.fifi.loading.Loadable`. Apps can keep local Loadable types or migrate gradually; app-specific error UI mapping stays in the app until you refactor Output mapping.

2. **ViewModel test helpers** — Prefer `com.paoapps.fifi.viewmodel.testOutput` / `testActions` over app-local copies. Existing app helpers can delegate to FiFi or be deleted after import path change.

3. **Dead code removed** — `multipartUpload` iOS stub removed from framework (was never part of public API).

4. **Android persistence** — `GlobalScope` replaced with app-scoped coroutine scope in `AndroidApp`; no app changes required.

5. **AppMainScope** — Uncaught coroutine errors now log via `com.paoapps.fifi.log.error`.

### Verification checklist

- [ ] `./gradlew shared:testDebugUnitTest` (or app equivalent)
- [ ] iOS: smoke test ViewModel screens; confirm `clear()` still called
- [ ] Android: cold start + environment switch (DataContainer persistence)
- [ ] Auth apps: token refresh flow

### Breaking changes

None identified between 0.0.44 and 0.0.45 for the framework + common (+ auth) surface. If compile errors appear, check Koin 4.x alignment (FiFi uses Koin 4.1.1).

### Staying current

Treat **FiftyFifty** as the canary consumer (full fifi-auth stack). Keep other consumers within one minor release of that pin.
