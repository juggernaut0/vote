package components.vote

import kui.*
import vote.api.v1.Response

class SelectOneAnswer(private val name: String, private val options: List<String>, private var selectedIndex: Int?) : AnswerPanelInput() {
    override fun createResponse(): Response {
        val selections = selectedIndex?.let { listOf(it) } ?: emptyList()
        return Response.selections(selections)
    }

    override fun render() {
        markup().div {
            for ((i, opt) in options.withIndex()) {
                val id = "$name-$i"
                div(classes("form-check")) {
                    radio(Props(id = id, classes = listOf("form-check-input")), name = name, value = i, model = ::selectedIndex)
                    label(classes("form-check-label"), forId = id) { +opt }
                }
            }
        }
    }
}
