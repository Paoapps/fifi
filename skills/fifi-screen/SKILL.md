---
name: fifi-screen
description: Scaffold a new KMP screen with FiFi AbstractViewModel (Output/Event/Action pattern).
---

# Add a FiFi Screen

Scaffolds a screen using FiFi's `AbstractViewModel<Output, Event, Action>` pattern.

## Workflow

### 1. Create abstract ViewModel

```kotlin
abstract class FeatureViewModel : AbstractViewModel<
    FeatureViewModel.Output,
    FeatureViewModel.Event,
    FeatureViewModel.Action
>() {
    data class Output(
        val title: String,
        val isLoading: Boolean = false,
    )

    sealed class Event : AbstractEvent() {
        data object Refresh : Event()
    }

    sealed class Action {
        data class Navigate(val route: String) : Action()
    }
}
```

### 2. Implement ViewModel

```kotlin
class FeatureViewModelImpl(
    private val model: FeatureModel,
) : FeatureViewModel() {

    private val _output = createRefreshableFetchFlow(model::data).map { cache ->
        Output(
            title = "Feature",
            isLoading = cache is CacheResult.Loading,
        )
    }

    override val output = _output.viewModelOutput(viewModelScope)

    override suspend fun handleEvent(event: Event) = when (event) {
        Event.Refresh -> { refresh(); null }
    }
}
```

### 3. Register in Koin

```kotlin
viewModel<FeatureViewModel> { FeatureViewModelImpl(get()) }
```

### 4. Platform UI

- **Android:** `ViewModelComposable(viewModel) { output -> /* Compose */ }`
- **iOS:** `ObservableViewModel` wrapper — call `viewModel.clear()` in `deinit`

### 5. Test

```kotlin
viewModel.testOutput { assertEquals("Feature", title) }
viewModel.testActions { emit -> emit(Event.Refresh) }
```

## References

- [docs/getting-started.md](../../docs/getting-started.md)
- [sample/shared/viewmodel/HomeViewModelImpl.kt](../../sample/shared/src/commonMain/kotlin/com/paoapps/fifi/sample/viewmodel/HomeViewModelImpl.kt)
- App-specific UI: use the **Definition pattern** in your app ([ADR 001](../../docs/decisions/001-framework-vs-app-boundary.md))
