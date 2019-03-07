package components.vote

import kui.*
import vote.api.v1.Response

class FreeformAnswer(private var text: String) : AnswerPanelInput() {
    override fun createResponse(): Response {
        return Response.freeform(text)
    }

    override fun render() {
        markup().inputText(classes("form-control"), model = ::text)
    }
}
