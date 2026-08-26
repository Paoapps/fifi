# Getting Started with FiFi

This guide walks through bootstrapping a new KMP app with FiFi. For auth-specific steps, continue with [auth-setup.md](auth-setup.md).

## Prerequisites

- Kotlin 2.3+ multiplatform project (Android + iOS)
- Jetpack Compose (Android) and SwiftUI (iOS) for UI
- Koin for dependency injection (included transitively via FiFi)

## 1. Add dependencies

In your **shared** module:

```kotlin
dependencies {
    api("com.paoapps.fifi:fifi-framework:0.0.45")
    api("com.paoapps.blockedcache:blocked-cache:0.0.10")
}
```

Optional server-shared types:

```kotlin
implementation("com.paoapps.fifi:fifi-common:0.0.45")
```

## 2. Implement AppDefinition

```kotlin
class MyAppDefinition(
    override val appVersion: String,
    override val isDebugMode: Boolean,
) : AppDefinition<MyEnvironment, MyApi> {

    override val environmentFactory = MyEnvironmentFactory()
    override fun apiFactory(appVersion: String) = MyApiFactory(appVersion, isDebugMode)
    override fun model() = MyModelImpl(/* scope */)

    override val modules = listOf(
        module {
            viewModel<HomeViewModel> { HomeViewModelImpl(get()) }
            // domain models, services…
        }
    )

    override fun dataRegistrations(): PersistentDataRegistry.() -> Unit = {
        registerPersistentData("appData", AppData.serializer(), AppData())
    }
}
```

Reference: [`sample/shared/.../SharedApp.kt`](../sample/shared/src/commonMain/kotlin/com/paoapps/fifi/sample/SharedApp.kt)

## 3. Initialize Koin

### Android

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinApp(
            context = this,
            appDefinition = MyAppDefinition(BuildConfig.VERSION_NAME, BuildConfig.DEBUG),
        )
    }
}
```

Uses `com.paoapps.fifi.koin.initKoinApp` — automatically persists `DataContainer` JSON to SharedPreferences per environment.

### iOS (common init)

```kotlin
// Called from Swift via exported framework
fun initSharedKoin(appDefinition: MyAppDefinition) {
    initKoinApp(appDefinition)
}
```

Use `com.paoapps.fifi.di.initKoinApp` from common code. See [ios-integration.md](ios-integration.md) for SwiftUI wiring and persistence.

## 4. Create a ViewModel

```kotlin
class HomeViewModelImpl : HomeViewModel() {
    private val model: HomeModel by inject()

    private val _output = createRefreshableFetchFlow(model::items).map { cache ->
        Output(items = cache.actualOrStaleData.orEmpty(), isLoading = cache is CacheResult.Loading)
    }

    override val output = _output.viewModelOutput(viewModelScope)

    override suspend fun handleEvent(event: Event) = when (event) {
        is Event.Open -> ActionHandler.EventResult.Action(Action.Navigate(event.id))
        else -> null
    }
}
```

## 5. Wire platform UI

### Android (Compose)

Use `com.paoapps.fifi.viewmodel.compose.ViewModelComposable` from `fifi-framework` androidMain, or bind manually:

```kotlin
val output by viewModel.output.collectAsState(initial = null)
LaunchedEffect(viewModel.action) { viewModel.action.collect(onAction) }
```

### iOS (SwiftUI)

Use `FlowAdapter.subscribe` or the patterns in [ios-integration.md](ios-integration.md). **Call `viewModel.clear()` in `deinit`.**

## 6. What stays in your app

FiFi does **not** include:

- Navigation graphs
- Design tokens (`ColorToken`, `FontToken`) — app conventions
- Full component libraries — use the **Definition** pattern in your app
- Brand-specific icons (`SealedImage` enums)

See [decisions/001-framework-vs-app-boundary.md](decisions/001-framework-vs-app-boundary.md).

## Checklist

- [ ] `fifi-framework` + `blocked-cache` on shared module classpath
- [ ] `AppDefinition` with environment, API factory, Model, modules
- [ ] `initKoinApp` from Android Application / iOS AppDelegate equivalent
- [ ] At least one `AbstractViewModel` with Output/Event/Action
- [ ] Platform UI collects `output` and `action`
- [ ] iOS: `clear()` on ViewModel teardown
- [ ] (Optional) Unit tests using `testOutput` / `testActions`

## Preview / test Koin

Use the same `initKoinApp` entry point as production. For auth apps, always use `com.paoapps.fifi.auth.di.initKoinApp` — do not mix with the non-auth overload in previews.
