package components.history

import components.CollapsibleAlert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import models.PollHistoryItem
import services.FetchException
import services.VoteService
import util.CHECK_MARK

class HistoryPage(private val service: VoteService) : Component() {
    private val alert = CollapsibleAlert("history-page-alert")

    private var created: List<PollHistoryItem> = emptyList()
    private var responded: List<PollHistoryItem> = emptyList()

    init {
        GlobalScope.launch {
            try {
                val (c, r) = service.getPollHistory()
                created = c
                responded = r
                render()
            } catch (e: FetchException) {
                console.error(e)
                alert.show("There was an error fetching poll history. (${e.status})")
            }
        }
    }

    override fun render() {
        markup().div {
            component(alert)

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
