package components.create

import components.create.QuestionPanel
import kui.Component
import kui.Props
import kui.classes
import util.X

class OptionItem(private val questionPanel: QuestionPanel) : Component() {
    var text: String = ""

    override fun render() {
        markup().li(classes("list-group-item", "d-flex")) {
            inputText(classes("form-control", "flex-grow-1", "w-auto"), ::text) // TODO attrs = mapOf("placeholder" to "Option...")
            button(Props(
                    classes = listOf("close", "ml-3"),
                    click = { questionPanel.removeOption(this@OptionItem) }
            )) { +X }
        }
    }
}
