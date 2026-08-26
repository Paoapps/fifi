import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var observable = ObservableViewModel(
        debugName: "Home",
        factory: { KoinKt.get(objCClass: HomeViewModelImpl.self) as! HomeViewModel }
    )

    var body: some View {
        NavigationStack {
            Group {
                if let output = observable.output {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(output.title)
                            .font(.headline)
                        if output.isLoading {
                            ProgressView()
                        }
                        if let item = output.item {
                            VStack(alignment: .leading) {
                                Text(item.title).font(.title2)
                                Text(item.description_).font(.body)
                            }
                            .onTapGesture {
                                observable.viewModel.emitEvent(event: item.onClick)
                            }
                        }
                        HStack {
                            ForEach(Array(output.buttons.enumerated()), id: \.offset) { _, button in
                                Button(button.title) {
                                    observable.viewModel.emitEvent(event: button.onClick)
                                }
                            }
                        }
                    }
                    .padding()
                } else {
                    ProgressView("Loading…")
                }
            }
            .navigationTitle("FiFi Sample")
        }
        .onChange(of: observable.action) { _, action in
            guard let action else { return }
            if let detail = action as? HomeViewModelActionDetail {
                print("Navigate to coffee \(detail.id)")
            }
        }
    }
}
