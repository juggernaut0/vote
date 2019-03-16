package components

import kui.*
import util.X

class CollapsibleAlert(private val id: String) : Component() {
    private var message = ""
    private val inner = componentOf {
        it.div(classes("alert", "alert-danger", "alert-dismissible")) {
            +message
            button(Props(classes = listOf("close"), click = { hide() })) { +X }
        }
    }

    fun show(msg: String) {
        message = msg
        inner.render()
        js("$")("#$id").collapse("show")
    }

    fun hide() {
        js("$")("#$id").collapse("hide")
    }

    override fun render() {
        markup().div(Props(id = id, classes = listOf("collapse", "my-2"))) {
            component(inner)
        }
    }
}
