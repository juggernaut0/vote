package components.vote

import kui.*
import vote.api.v1.Question
import vote.api.v1.QuestionType
import vote.api.v1.Response
import vote.api.v1.SelectSubtype
import kotlin.random.Random

class AnswerPanel(private val question: Question) : Component() {
    private val input: AnswerPanelInput = when(val t = question.type) {
        QuestionType.FREEFORM -> {
            FreeformAnswer()
        }
        QuestionType.SELECT -> {
            if (question.subtype == SelectSubtype.SELECT_ONE) {
                SelectOneAnswer(genName(), question.options)
            } else {
                SelectManyAnswer(genName(), question.options)
            }
        }
        QuestionType.RANKED -> {
            RankedAnswer(question.options)
        }
        else -> throw IllegalArgumentException("Unknown type: $t")
    }

    fun createResponse(): Response = input.createResponse()

    override fun render() {
        markup().div(classes("card", "bg-light", "mb-2")) {
            div(classes("card-body")) {
                h5 { +question.question }
                component(input)
            }
        }
    }

    companion object {
        @UseExperimental(ExperimentalUnsignedTypes::class)
        private fun genName(): String {
            return Random.nextInt().toUInt().toString(36)
        }
    }
}
