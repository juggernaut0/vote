package components.create

import components.MultiInput
import kui.*
import util.*
import vote.api.v1.*

class QuestionPanel(private val createPage: CreatePage) : Component() {
    private var question: String = ""
    private var type: QType by renderOnSet(QType.FREEFORM)
    private val options = MultiInput(placeholder = "Add option...")

    private fun isFirst(): Boolean = createPage.isFirst(this)
    private fun isLast(): Boolean = createPage.isLast(this)
    private fun moveUp() = createPage.moveUp(this)
    private fun moveDown() = createPage.moveDown(this)
    private fun removeThis() = createPage.removeQuestion(this)

    fun createQuestion(): Question {
        val opts = if (type == QType.FREEFORM) {
            emptyList()
        } else {
            options.getValues()
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
                        if (type.type != QuestionType.FREEFORM) {
                            label { +"Options" }
                            component(options)
                        }
                    }
                }
                div(classes("question-buttons", "ml-2", "border-left")) {
                    button(Props(
                            classes = listOf("close", "question-button", "invisible-disabled"),
                            click = { moveUp() },
                            disabled = isFirst()
                    )) { +UP_ARROW }
                    button(Props(
                            classes = listOf("close", "question-button"),
                            click = { removeThis() }
                    )) { +X }
                    button(Props(
                            classes = listOf("close", "question-button", "invisible-disabled"),
                            click = { moveDown() },
                            disabled = isLast()
                    )) { +DOWN_ARROW }
                }
            }
        }
    }

    private enum class QType(val type: String, val subtype: String) {
        FREEFORM(QuestionType.FREEFORM, FreeformSubtype.SINGLE),
        FREEFORM_MUTLIPLE_ANSWER(QuestionType.FREEFORM, FreeformSubtype.MUTLI),
        SELECT_ONE(QuestionType.SELECT, SelectSubtype.SELECT_ONE),
        SELECT_MANY(QuestionType.SELECT, SelectSubtype.SELECT_MANY),
        BORDA_COUNT(QuestionType.RANKED, RankedSubtype.BORDA_COUNT),
        INSTANT_RUNOFF(QuestionType.RANKED, RankedSubtype.INSTANT_RUNOFF),
        RANGE_VOTING(QuestionType.RANGE, ""),
        ;

        override fun toString(): String {
            return name.split('_').joinToString(separator = " ") { it.toLowerCase().capitalize() }
        }
    }
}
