package components

import kui.*

class Checkbox(private val id: String, private val label: String, selected: Boolean = false) : Component() {
    var selected: Boolean by renderOnSet(selected)

    override fun render() {
        markup().div(classes("form-check")) {
            checkbox(Props(id = id, classes = listOf("form-check-input")), model = ::selected)
            label(classes("form-check-label"), forId = id) { +label }
        }
    }
}
