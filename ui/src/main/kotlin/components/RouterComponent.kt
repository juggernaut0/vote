package components

import kui.*

class RouterComponent<TState>(
        initialState: TState,
        private val compFn: (TState) -> Component
) : Component() {
    var state: TState by renderOnSet(initialState)

    override fun render() {
        markup().div {
            component(compFn(state))
        }
    }
}
