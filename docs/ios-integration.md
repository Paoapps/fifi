# iOS Integration

FiFi shared code exports to an iOS framework via Kotlin/Native. Platform UI uses SwiftUI and bridges FiFi `Flow`s to `@Published` properties.

## Initialization

From Swift, call the exported Koin initializer:

```swift
let appDefinition = SharedAppDefinition(
    appVersion: Bundle.main.appVersion,
    isDebugMode: _isDebugAssertConfiguration()
)
KoinKt.doInitKoinApp(appDefinition: appDefinition, additionalModules: [], logger: nil) {
    // optional KoinAppDeclaration
}
```

For auth apps, use the auth module's exported `initKoinApp`.

## ViewModel lifecycle

Unlike Android, iOS ViewModels use `AppMainScope()` — **not** tied to SwiftUI view lifecycle automatically.

**Required:** call `clear()` when the observable wrapper is deallocated:

```swift
deinit {
    viewModel.clear()
}
```

See `sample/iosApp/iosApp/ObservableViewModel.swift` for a reference wrapper.

## FlowAdapter bridging

`FlowAdapter` exposes `subscribe(onEach:onComplete:onThrow:)` for imperative collection:

```swift
viewModel.output.subscribe(
    onEach: { output in
        DispatchQueue.main.async { self.output = output }
    },
    onComplete: { },
    onThrow: { error in print(error) }
)
```

Collect `action`, `globalActions`, and `confirmationDialogs` the same way.

## Persistence

Android persists `DataContainer` JSON automatically via `AndroidApp`. **iOS has no built-in disk persistence.**

Implement app-side storage:

1. Observe `model.dataContainers` flows
2. Serialize `CDataContainer.json` to files or UserDefaults
3. Restore via `updateJson()` on startup

Consumer apps typically ship a Swift `DataStore` type for this — copy that pattern; do not expect FiFi to provide Keychain/file IO on iOS.

## Foreground refresh

Call `appBecameActive()` (from `ModelUtils`) when the app enters foreground to trigger cache refresh policies your Models define.

## Archive / type identity

KMP `Output` types may require explicit casts in Release builds when generic identity erases differently than Debug. Document workarounds in your app if needed (see FiftyFifty's iOS archive notes).

## iOS sample

The `sample/iosApp` folder contains a minimal SwiftUI app that links the `shared` framework built from `sample/shared`. See its README for Xcode setup steps.

## Compose parity

Android apps can use `com.paoapps.fifi.viewmodel.compose.ViewModelComposable` (in fifi-framework androidMain). SwiftUI apps use the Observable wrapper pattern above — FiFi intentionally does not ship SwiftUI components.
