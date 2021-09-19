package components.vote

import components.MultiInput
import vote.api.v1.Response

class MultiFreeformAnswer(values: List<String>) : AnswerPanelInput() {
    private val input = MultiInput(initial = values, placeholder = "Add a response...")

    override fun createResponse(): Response {
        return Response.multiFreeform(input.getValues())
    }

    override fun render() {
        markup().component(input)
    }
}