package components

import kui.*
import util.X

class MultiInput(initial: List<String> = emptyList(), private val placeholder: String? = null) : Component() {
    private val items: MutableList<Item> = initial.mapTo(mutableListOf()) { Item(it) }.also { it.add(Item()) }

    fun getValues(): List<String> = items.mapNotNull { item -> item.text.takeUnless { it.isBlank() } }

    override fun render() {
        markup().ul(classes("list-group", "mb-2")) {
            for (item in items) {
                component(item)
            }
        }
    }

    private inner class Item(text: String = "") : Component() {
        var text: String = text
            private set(value) {
                val renderTarget =
                        if (field.isBlank() && value.isNotBlank() && isLast()) {
                            items.add(Item())
                            this@MultiInput
                        } else {
                            this
                        }
                field = value
                renderTarget.render()
            }

        private fun removeThis() {
            if (isLast()) return
            items.remove(this)
            this@MultiInput.render()
        }

        private fun isLast(): Boolean {
            return items.last() == this
        }

        override fun render() {
            markup().li(classes("list-group-item", "d-flex")) {
                inputText(
                        Props(
                                classes = listOf("form-control", "flex-grow-1"),
                                blur = { if (text.isBlank()) removeThis() }
                        ),
                        placeholder = placeholder,
                        model = ::text)
                button(Props(
                        classes = listOf("close", "invisible-disabled", "ml-3"),
                        click = { removeThis() },
                        disabled = isLast()
                )) { +X }
            }
        }
    }
}
