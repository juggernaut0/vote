package components

import kui.*
import util.X

class MultiInput(
    initial: List<String> = emptyList(),
    private val placeholder: String? = null,
) : Component() {
    private var newItemId = initial.size
    private val items: MutableList<Item> = initial.mapIndexedTo(mutableListOf()) { i, v -> Item(i, v) }.also { it.add(Item(newItemId++)) }

    fun getValues(): List<String> = items.mapNotNull { item -> item.text.takeUnless { it.isBlank() } }

    override fun render() {
        markup().ul(classes("list-group", "mb-2")) {
            for (item in items) {
                component(item)
            }
        }
    }

    private inner class Item(private val index: Int, text: String = "") : Component() {
        var text: String = text
            private set(value) {
                val renderTarget =
                        if (field.isBlank() && value.isNotBlank() && isLast()) {
                            items.add(Item(newItemId++))
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
            // id is used so that kui consistently renders the same item in the same position
            // otherwise focus would jump around as items are added and removed
            // Yes I know id is not unique if there are multiple MultiInputs on the page
            markup().li(Props(id = index.toString(), classes = listOf("list-group-item", "d-flex"))) {
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
