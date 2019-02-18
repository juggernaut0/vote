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

    fun createQuestion(): Question {
        return Question(
                question = question,
                type = type.type,
                subtype = type.subtype,
                options = if (type == QType.FREEFORM) emptyList() else options.map { it.text }
        )
    }

    override fun render() {
        markup().div(classes("card", "bg-light", "mb-2")) {
            div(classes("card-body")) {
                labelledTextInput("Question", ::question)
                labelledDropdown("Type", ::type, QType.values().asList())
                if (type != QType.FREEFORM) {
                    label { +"Options" }
                    ul(classes("list-group", "mb-2")) {
                        for (opt in options) {
                            component(opt)
                        }
                        button(Props(
                                classes = listOf("list-group-item", "list-group-item-action", "active", "text-center"),
                                click = { addOption() }
                        )) { +"Add Option" }
                    }

                }
                button(Props(
                        classes = listOf("btn", "btn-danger", "btn-block"),
                        click = { createPage.removeQuestion(this@QuestionPanel) }
                )) { +"Remove Question" }
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
