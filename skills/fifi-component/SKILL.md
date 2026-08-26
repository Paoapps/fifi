---
name: fifi-component
description: Create a cross-platform UI Definition contract for FiFi apps (app-owned design system).
---

# Add a FiFi Component Definition

FiFi ships minimal primitives (`TextDefinition`, `ConfirmationDialogDefinition`). **Full components live in your app** using the Definition pattern.

## Template

```kotlin
object ArticleRowDefinition {
    data class Properties<Event>(
        val title: TextDefinition.Properties,
        val subtitle: TextDefinition.Properties? = null,
        val onClick: Event,
    )

    data class Style(
        val padding: Dp = 16.dp,
    )
}
```

## Rules

1. **Properties** hold display data + typed `Event` for interactions
2. **Style** holds layout/visual defaults (app tokens — not FiFi exports)
3. ViewModels build `Properties` in `Output`
4. Platform `*Component.kt` / `*Component.swift` render Properties

## FiFi primitives to reuse

- `com.paoapps.fifi.ui.component.TextDefinition`
- `com.paoapps.fifi.ui.component.ConfirmationDialogDefinition`
- `LightAndDarkColor`, `StateColor`, `AttributedText`

## Do not put in FiFi

- `ColorToken`, `FontToken`, `SealedImage` — app-owned
- App-specific component catalogs

See [docs/decisions/001-framework-vs-app-boundary.md](../../docs/decisions/001-framework-vs-app-boundary.md).
