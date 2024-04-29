# FiFi: Kotlin Multiplatform Mobile Framework

FiFi is a Kotlin Multiplatform Mobile (KMM) framework designed to facilitate maximum code sharing between iOS and Android, focusing on almost everything except UI and navigation code. With FiFi, you can write network requests, caching mechanisms, view models, business logic, and event handling all in shared code, allowing for a seamless development experience across platforms.

## EARLY DEVELOPMENT!

FiFi is currently in early development. The API is not stable and is subject to change. We are actively working on the framework and will be working on documentation and examples in the near future.

We encourage you to try out FiFi and provide feedback. If you have any questions, please reach out to us.

## Key Features
* **Maximum Code Sharing**: Write most of your app logic once and run on both iOS and Android.
* **Built on Proven Libraries**: Utilizes Kotlin serialization, Kotlin date time, Ktor, and Koin.
* **Two-Module Architecture**: `fifi-common` can be shared with projects like a Ktor-based server, while `fifi-framework` is dedicated for mobile apps.
* **Highly Compatible with Modern UI**: Tailored to work efficiently with SwiftUI and Compose.

## Prerequisites
* Kotlin version 1.9.23
* Familiarity with Kotlin Multiplatform Mobile setup [KMM Getting Started](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
* Strong recommendation to use SwiftUI for iOS and Compose for Android.

## What is FiFi?

FiFi is a great fit for apps that communicate with a server (optionally with token based authentication) and need to cache some of that data. Based on that data, view models can be created and used to drive the UI automatically.

### View Models

The framework lets your create shared view models in Kotlin. These view models define in an **Output** data class what a view needs to display. It also provides **Event**s that the UI can send to the view model. The view model can then react to these events and update the output accordingly. Whenever the output changes, the UI is automatically updated. Whenever the UI needs to react to an event, the view model is notified through an **Action**.

### Models

View Models get their data through Models. Models are responsible for determining when to fetch data from the server and when to use cached data. A view model generally only indicates what data it needs and the model will take care of the rest.

### Caching

Models keep a cache of a single data structure tree in memory. This acts as the single source of truth for the app. Whenever a view model needs data, it can request it from the model. The model will then either return the cached data or fetch it from the server. The model also keeps track of the last time it fetched data from the server. If the data is older than a certain threshold, the model will automatically fetch new data from the server. Besides having the cache in memory it allows iOS and Android implementations to store the cache on disk as well.

### API's

The framework provides a way to define API's in Kotlin. These API's can be used to make network requests to a server. The framework provides a way to automatically parse the response from the server into Kotlin data classes. The framework also provides a way to automatically parse errors from the server into Kotlin data classes. This allows you to define a single API for both iOS and Android and use it in your shared code.

### Authentication

The framework provides a way to authenticate with a server using tokens. It provides a way to automatically refresh tokens when they expire. Authentication is optional and can be enabled by using the `fifi-auth` module.

## Installation

Set up a Kotlin Multiplatform Mobile (KMM) project as outlined [here](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html).

In your shared module, create a dependency to the `fifi-framework` library.

```kotlin
implementation("com.paoapps.fifi:fifi-framework:0.0.27")
```

## Setup

Implement an `initApp` function in your shared code. This function should be called from both Android and iOS main entry points.
```kotlin
fun <ModelData, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims, ServerError, Api: ClientApi<AccessTokenClaims>> initKoinShared(
    serviceName: String,
    sharedAppModule: Module,
    model: () -> Model<ModelData, AccessTokenClaims, Environment, UserId, Api>,
    tokenDecoder: TokenDecoder<AccessTokenClaims, RefreshTokenClaims>,
    authApi: (scope: Scope) -> AuthApi<ServerError>,
    languageProvider: LanguageProvider,
    stringsProvider: CommonStringsProvider,
    serverErrorParser: ServerErrorParser<ServerError>,
    appDeclaration: KoinAppDeclaration = {}
)
```

# Contributing

We welcome contributions to FiFi. Please read our [contributing guide](CONTRIBUTING.md) for more information.