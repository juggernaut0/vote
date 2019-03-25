package components.history

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import models.PollHistoryItem
import services.VoteService
import util.CHECK_MARK

class HistoryPage(private val service: VoteService) : Component() {
    private var created: List<PollHistoryItem> = emptyList()
    private var responded: List<PollHistoryItem> = emptyList()

    init {
        GlobalScope.launch {
            val (c, r) = service.getPollHistory()
            created = c
            responded = r
            render()
        }
    }

    override fun render() {
        markup().div {
            button(Props(classes = listOf("btn", "btn-success"), click = { service.goToCreatePage() })) {
                +"Create New Poll"
            }

            if (created.isNotEmpty()) {
                h3 { +"Polls you've created" }
                ul {
                    for (phi in created) {
                        li {
                            a(href = "/?results=${phi.id}") { +phi.title }
                            if (phi.responded) {
                                span(classes("text-success")) { +" $CHECK_MARK" }
                            }
                        }
                    }
                }
            }

            if (responded.isNotEmpty()) {
                h3 { +"Polls you've responded to" }
                ul {
                    for (phi in responded) {
                        li {
                            a(href = "/?results=${phi.id}") { +phi.title }
                        }
                    }
                }
            }
        }
    }
}
