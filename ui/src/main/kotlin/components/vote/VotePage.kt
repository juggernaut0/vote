package components.vote

import components.CollapsibleAlert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollToOptions
import services.FetchException
import services.VoteService
import vote.api.v1.Poll
import vote.api.v1.PollResponse
import kotlin.browser.window

class VotePage(private val service: VoteService) : Component() {
    private val alert = CollapsibleAlert("vote-page-alert")

    private var poll: Poll? = null
    private var answers: List<AnswerPanel> = emptyList()

    private var submitting = false
    private var notFound = false

    init {
        GlobalScope.launch {
            try {
                val (p, resp) = service.getCurrentPoll()
                if (p == null) notFound = true
                else {
                    poll = p
                    answers = p.questions.zip(resp?.responses.orNulls()).map { (q, r) -> AnswerPanel(q, r) }
                }
                render()
            } catch (e: FetchException) {
                console.error(e)
                alert.show("There was an error fetching the poll. (${e.status})")
            }
        }
    }

    private fun submit() {
        submitting = true
        render()
        val response = PollResponse(
                responses = answers.map { it.createResponse() }
        )
        GlobalScope.launch {
            try {
                service.submitResponse(poll!!.id, response)
            } catch (e: FetchException) {
                console.error(e)
                window.scrollTo(ScrollToOptions(0.0, 0.0, ScrollBehavior.SMOOTH))
                alert.show("There was an error submitting your response. (${e.status})")
            }
        }
    }

    override fun render() {
        val poll = poll
        markup().div {
            component(alert)
            if (poll != null) {
                h3 { +poll.title }
                for (a in answers) {
                    component(a)
                }
                button(Props(
                        classes = listOf("btn", "btn-success", "btn-block"),
                        click = { submit() },
                        disabled = submitting
                )) {
                    if (submitting) {
                        +"Submitting..."
                    } else {
                        +"Submit"
                    }
                }
            } else if(notFound) {
                p { +"Oopsy woopsy! Poll not found! >_<" }
            } else {
                p { +"Loading poll..." }
            }
        }
    }

    private fun <T> Iterable<T>?.orNulls(): Iterable<T?> {
        return this ?: Iterable { object : Iterator<T?> {
            override fun hasNext(): Boolean = true
            override fun next(): T? = null
        } }
    }
}
