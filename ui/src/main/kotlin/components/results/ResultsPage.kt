package components.results

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import services.VoteService
import vote.api.v1.Poll
import vote.api.v1.PollResults
import vote.api.v1.QuestionType

class ResultsPage(service: VoteService) : Component() {
    private var pollResults: Pair<Poll, PollResults>? = null

    private var notFound = false

    init {
        GlobalScope.launch {
            pollResults = service.getResults()
            if (pollResults == null) {
                notFound = true
            }
            render()
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
            if (pr != null) {
                val (poll, results) = pr
                h3 { +"${poll.title} - Results" }
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
