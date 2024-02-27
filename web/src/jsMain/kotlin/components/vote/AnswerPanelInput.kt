package components.vote

import kui.Component
import vote.api.v1.Response

abstract class AnswerPanelInput : Component() {
    abstract fun createResponse(): Response
}
