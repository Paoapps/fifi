# Auth Setup (fifi-auth)

Use `fifi-auth` when your app needs token-based authentication with automatic refresh.

## Dependencies

```kotlin
implementation("com.paoapps.fifi:fifi-auth:0.0.45")
// transitively includes fifi-framework and fifi-common
```

## 1. Define claim types

```kotlin
@Serializable
data class AccessClaims(
    override val sub: String,
    override val exp: Instant,
) : IdentifiableClaims<String>

@Serializable
data class RefreshClaims(
    override val exp: Instant,
) : Claims
```

## 2. Implement TokenDecoder

```kotlin
class MyTokenDecoder : TokenDecoder<String, AccessClaims, RefreshClaims> {
    override fun accessTokenClaims(accessToken: String) = /* decode JWT */
    override fun refreshTokenClaims(refreshToken: String) = /* decode JWT */
    override fun encodeAccessTokenClaims(claims: AccessClaims) = /* encode */
    override fun encodeRefreshTokenClaims(claims: RefreshClaims) = /* encode */
}
```

## 3. Implement AuthAppDefinition

```kotlin
class MyAuthAppDefinition(
    override val appVersion: String,
    override val isDebugMode: Boolean,
    override val serviceName: String,
    override val authentication: Authentication<UserId, AccessClaims, RefreshClaims>,
) : AuthAppDefinition<MyEnvironment, UserId, AccessClaims, RefreshClaims, MyAuthApi> {

    override val environmentFactory = MyEnvironmentFactory()
    override fun apiFactory(appVersion: String) = MyAuthApiFactory(appVersion, isDebugMode)
    override fun model() = MyAuthModelImpl(/* scope */)
    override val modules = listOf(/* viewModels, services */)

    override fun authModel(model: Model<MyEnvironment, MyAuthApi>) =
        AuthModelImpl(model)
}
```

## 4. Initialize Koin

**Always use the auth entry point:**

```kotlin
import com.paoapps.fifi.auth.di.initKoinApp

initKoinApp(authAppDefinition = myAuthAppDefinition)
```

Android with context (via fifi-auth Android extensions or app wrapper):

```kotlin
// Pattern used by FiftyFifty — wrap auth init after platform module registration
initKoinApp(context, authAppDefinition) { /* androidContext, etc. */ }
```

## 5. Protected API calls

Extend `AuthClientApiImpl` and use `AuthApiHelper.authenticated { }`:

```kotlin
authApiHelper.authenticated("fetchProfile") { token, claims ->
    client.get { /* Authorization: Bearer $token */ }
}
```

## Platform requirements

- **Android:** Encrypted `Settings` injected via `platformInjections` (requires `Context`)
- **iOS:** Keychain-backed token store with migration fallback (see fifi-auth iosMain)

## Non-auth apps

Apps that use a different auth stack can use `fifi-framework` alone and skip `fifi-auth`.

Reference consumer: FiftyFifty Card (`ClientAppDefinition` extends `AuthAppDefinition`).
