import Combine
import SwiftUI
import shared

/// Bridges FiFi [AbstractViewModel] Flows to SwiftUI `@Published` properties.
/// Call `viewModel.clear()` on teardown — handled in `deinit`.
final class ObservableViewModel<
    VM: AbstractViewModel<Output, Event, Action>,
    Output: AnyObject,
    Event: AnyObject,
    Action: AnyObject
>: ObservableObject {
    @Published var output: Output?
    @Published var action: Action?

    let viewModel: VM
    private var cancellables = Set<AnyCancellable>()
    private let debugName: String

    init(debugName: String, factory: () -> VM) {
        self.debugName = debugName
        self.viewModel = factory()
        bind()
    }

    deinit {
        viewModel.clear()
    }

    private func bind() {
        viewModel.viewModelOutput.subscribe(
            onEach: { [weak self] update in
                DispatchQueue.main.async {
                    self?.output = update.output as? Output
                }
            },
            onComplete: { },
            onThrow: { error in print("\(self.debugName): output error \(error)") }
        ).store(in: &cancellables)

        viewModel.action.subscribe(
            onEach: { [weak self] action in
                DispatchQueue.main.async {
                    self?.action = action as? Action
                }
            },
            onComplete: { },
            onThrow: { error in print("\(self.debugName): action error \(error)") }
        ).store(in: &cancellables)
    }
}

private extension Canceller {
    func store(in set: inout Set<AnyCancellable>) {
        set.insert(AnyCancellable { self.cancel() })
    }
}
