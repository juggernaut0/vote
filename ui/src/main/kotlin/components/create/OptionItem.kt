package components.create

import kui.Component
import kui.Props
import kui.classes
import util.X

class OptionItem(private val questionPanel: QuestionPanel) : Component() {
    var text: String = ""

    override fun render() {
        markup().li(classes("list-group-item", "d-flex")) {
            inputText(classes("form-control", "flex-grow-1"), placeholder = "Add Option...", model = ::text)
            button(Props(
                    classes = listOf("close", "ml-3"),
                    click = { questionPanel.removeOption(this@OptionItem) }
            )) { +X }
        }
    }
}
