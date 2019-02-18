package components.vote

import kui.*
import vote.api.v1.Response

class FreeformAnswer : AnswerPanelInput() {
    private var text: String = ""

    override fun createResponse(): Response {
        return Response.freeform(text)
    }

    override fun render() {
        markup().inputText(classes("form-control"), ::text)
    }
}
