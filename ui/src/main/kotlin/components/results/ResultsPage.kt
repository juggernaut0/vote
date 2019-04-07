package components.results

import components.CollapsibleAlert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import services.FetchException
import services.VoteService
import vote.api.v1.Poll
import vote.api.v1.PollResults
import vote.api.v1.QuestionType

class ResultsPage(private val service: VoteService) : Component() {
    private val alert = CollapsibleAlert("results-page-alert")

    private var pollResults: Pair<Poll, PollResults>? = null
    private var isOwner: Boolean = false

    private var notFound = false

    init {
        refresh()
    }

    private fun refresh() {
        GlobalScope.launch {
            try {
                pollResults = service.getResults()
                if (pollResults == null) {
                    notFound = true
                }
                render()
                isOwner = pollResults?.let { service.isCreator(it.first.id) } ?: false
                if (isOwner) {
                    render()
                }
            } catch (e: FetchException) {
                console.error(e)
                alert.show("There was an error fetching poll results. (${e.status})")
            }
        }
    }

    private fun MarkupBuilder.freeform(responses: List<String>) {
        ul {
            for (s in responses.sorted()) {
                li { +s }
            }
        }
    }

    private fun MarkupBuilder.votes(votes: List<Pair<String, Int>>) {
        val max = votes.map { it.second }.max()?.takeIf { it > 0 }?.toDouble() ?: Double.POSITIVE_INFINITY
        div(classes("p-1")) {
            for ((opt, vs) in votes.sortedBy { -it.second }) {
                val w = vs / max * 100
                div(classes("border", "my-1", "text-nowrap", "overflow-hidden")) {
                    div(Props(classes = listOf("p-2", "vote-bar"), attrs = mapOf("style" to "width: $w%;"))) {
                        +"$vs - $opt"
                    }
                }
            }
        }
    }

    override fun render() {
        val pr = pollResults
        markup().div {
            component(alert)
            if (pr != null) {
                val (poll, results) = pr
                div(classes("d-flex", "justify-content-between")) {
                    h3 { +"${poll.title} - Results" }
                    button(Props(classes = listOf("close"), click = { refresh() })) { +"\u21bb" }
                }
                if (isOwner) {
                    a(Props(click = { service.goToDetailsPage() })) { +"View responses" }
                }
                for ((q, r) in poll.questions.zip(results.results)) {
                    div(classes("card", "bg-light", "mb-2")) {
                        div(classes("card-body")) {
                            h5 { +q.question }
                            small(classes("text-muted")) {
                                val n = r.responseCount
                                val pl = if (n == 1) "" else "s"
                                +" $n response$pl"
                            }
                            when (q.type) {
                                QuestionType.FREEFORM -> freeform(r.freeform!!)
                                else -> votes(q.options.zip(r.votes!!))
                            }
                        }
                    }
                }
            } else if (notFound) {
                p { +"Oopsy woopsy! Poll not found! >_<" }
            } else {
                p { +"Fetching results..." }
            }
        }
    }
}
