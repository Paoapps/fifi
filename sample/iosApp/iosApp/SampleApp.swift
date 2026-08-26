import SwiftUI
import shared

@main
struct SampleApp: App {
    init() {
        let definition = SharedAppDefinition(
            appVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0",
            isDebugMode: _isDebugAssertConfiguration()
        )
        KoinKt.doInitKoinApp(appDefinition: definition, additionalModules: [], logger: nil) { _ in }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
