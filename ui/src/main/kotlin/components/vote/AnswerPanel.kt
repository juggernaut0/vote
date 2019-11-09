package components.vote

import kui.*
import vote.api.v1.*
import kotlin.random.Random

class AnswerPanel(private val question: Question, existing: Response?) : Component() {
    private val input: AnswerPanelInput = when(val t = question.type) {
        QuestionType.FREEFORM -> {
            if (question.subtype == FreeformSubtype.MUTLI) {
                MultiFreeformAnswer(existing?.multiFreeform.orEmpty())
            } else {
                FreeformAnswer(existing?.freeform ?: "")
            }
        }
        QuestionType.SELECT -> {
            if (question.subtype == SelectSubtype.SELECT_ONE) {
                SelectOneAnswer(genName(), question.options, existing?.selections?.getOrNull(0))
            } else {
                SelectManyAnswer(genName(), question.options, existing?.selections.orEmpty().toSet())
            }
        }
        QuestionType.RANKED -> RankedAnswer(question.options, existing?.selections.orEmpty())
        QuestionType.RANGE -> RangeAnswer(question.options, existing?.selections ?: List(question.options.size) { 50 })
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
