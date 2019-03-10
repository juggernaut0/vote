package components.create

import kui.*
import util.labelledDropdown
import util.labelledTextInput
import vote.api.v1.Question
import vote.api.v1.QuestionType
import vote.api.v1.RankedSubtype
import vote.api.v1.SelectSubtype

class QuestionPanel(private val createPage: CreatePage) : Component() {
    private var question: String = ""
    private var type: QType by renderOnSet(QType.FREEFORM)
    private val options: MutableList<OptionItem> = mutableListOf(OptionItem(this))

    private fun addOption() {
        options.add(OptionItem(this))
        render()
    }

    fun removeOption(opt: OptionItem) {
        options.remove(opt)
        render()
    }

    private fun isFirst(): Boolean = createPage.isFirst(this)
    private fun isLast(): Boolean = createPage.isLast(this)
    private fun moveUp() = createPage.moveUp(this)
    private fun moveDown() = createPage.moveDown(this)
    private fun removeThis() = createPage.removeQuestion(this)

    fun createQuestion(): Question {
        val opts = if (type == QType.FREEFORM) {
            emptyList()
        } else {
            options.mapNotNull { o -> o.text.takeUnless { it.isBlank() } }
        }
        return Question(
                question = question,
                type = type.type,
                subtype = type.subtype,
                options = opts
        )
    }

    override fun render() {
        markup().div(classes("card", "bg-light", "mb-2")) {
            div(classes("d-flex")) {
                div(classes("flex-fill")) {
                    div(classes("card-body")) {
                        labelledTextInput("Question", ::question)
                        labelledDropdown("Type", ::type, QType.values().asList())
                        if (type != QType.FREEFORM) {
                            label { +"Options" }
                            ul(classes("list-group", "mb-2")) {
                                /* TODO: When an empty OptionItem has something typed in it, add a new empty one beneath it
                                 * When an empty OptionItem loses focus, remove it automatically, except if it's the last one in the list
                                 * Remove/disable X button on last OptionItem in list
                                 * Then the "Add Option" button can be removed
                                 */
                                for (opt in options) {
                                    component(opt)
                                }
                                button(Props(
                                        classes = listOf("list-group-item", "list-group-item-action", "active", "text-center"),
                                        click = { addOption() }
                                )) { +"Add Option" }
                            }

                        }
                    }
                }
                div(classes("question-buttons", "ml-2", "border-left")) {
                    button(Props(
                            classes = listOf("close", "question-button"),
                            click = { moveUp() },
                            disabled = isFirst()
                    )) { +"\u25B2" }
                    button(Props(
                            classes = listOf("close", "question-button"),
                            click = { removeThis() }
                    )) { +"\u00D7" }
                    button(Props(
                            classes = listOf("close", "question-button"),
                            click = { moveDown() },
                            disabled = isLast()
                    )) { +"\u25BC" }
                }
            }
        }
    }

    private enum class QType(val type: String, val subtype: String) {
        FREEFORM(QuestionType.FREEFORM, ""),
        SELECT_ONE(QuestionType.SELECT, SelectSubtype.SELECT_ONE),
        SELECT_MANY(QuestionType.SELECT, SelectSubtype.SELECT_MANY),
        BORDA_COUNT(QuestionType.RANKED, RankedSubtype.BORDA_COUNT),
        INSTANT_RUNOFF(QuestionType.RANKED, RankedSubtype.INSTANT_RUNOFF);

        override fun toString(): String {
            return name.split('_').joinToString(separator = " ") { it.toLowerCase().capitalize() }
        }
    }
}
