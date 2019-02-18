package services

import components.RouterComponent
import kui.Component
import kotlin.browser.window

class Router<TState>(private val initialState: TState, stateCast: (Any?) -> TState?) {
    private lateinit var component: RouterComponent<TState>
    var state: TState
        get() = component.state
        set(value) {
            component.state = value
        }

    init {
        window.onpopstate = {
            val st = stateCast(it.state)
            if (st != null) {
                component.state = st
            }
        }
    }

    fun component(compFn: (TState) -> Component): RouterComponent<TState> {
        component = RouterComponent(initialState, compFn)
        return component
    }
}