# FiFi iOS Sample

Minimal SwiftUI app demonstrating FiFi bootstrap and ViewModel observation.

## Prerequisites

- Xcode 15+
- Kotlin `shared` framework from `sample/shared`

## Setup

1. Build the shared framework:

   ```bash
   ./gradlew :sample:shared:linkDebugFrameworkIosSimulatorArm64
   ```

2. Create or open an Xcode iOS App project in this folder and add the Swift files under `iosApp/`.

3. Link `shared.framework` from `sample/shared/build/bin/iosSimulatorArm64/debugFramework/`.

4. Run on iOS Simulator.

See [docs/ios-integration.md](../../docs/ios-integration.md) for full guidance.
