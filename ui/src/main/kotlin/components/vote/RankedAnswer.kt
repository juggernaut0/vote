package components.vote

import kui.*
import vote.api.v1.Response

class RankedAnswer(private val options: List<String>) : AnswerPanelInput() {
    private val ranked: MutableList<Int> = mutableListOf()

    override fun createResponse(): Response {
        return Response.selections(ranked)
    }

    private fun rankItem(oi: Int) {
        ranked.add(oi)
        render()
    }

    private fun unrankItem(ri: Int) {
        ranked.removeAt(ri)
        render()
    }

    private fun btnClass(disabled: Boolean): List<String> {
        return if (disabled) {
            listOf("list-group-item", "list-group-item-action", "bg-secondary", "text-light")
        } else {
            listOf("list-group-item", "list-group-item-action")
        }
    }

    override fun render() {
        markup().div(classes("row")) {
            div(classes("col-12")) {
                p { +"Click to rank items (highest to lowest)" }
            }
            div(classes("col-6", "list-group")) {
                for ((i, opt) in options.withIndex()) {
                    val disabled = i in ranked
                    button(Props(
                            classes = btnClass(disabled),
                            click = { rankItem(i) },
                            disabled = disabled
                    )) {
                        +opt
                    }
                }
            }
            div(classes("col-6", "list-group")) {
                for ((i, oi) in ranked.withIndex()) {
                    button(Props(
                            classes = listOf("list-group-item", "list-group-item-action"),
                            click = { unrankItem(i) }
                    )) {
                        +options[oi]
                    }
                }
            }
        }
    }
}
