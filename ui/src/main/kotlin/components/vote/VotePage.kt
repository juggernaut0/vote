package components.vote

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import services.GoogleSignIn
import services.VoteService
import vote.api.v1.Poll
import vote.api.v1.PollResponse

class VotePage(private val service: VoteService, private val signIn: GoogleSignIn) : Component() {
    private var poll: Poll? = null
    private var response: PollResponse? = null
    private var answers: List<AnswerPanel> = emptyList()

    private var submitting = false
    private var notFound = false

    init {
        GlobalScope.launch {
            val (p, resp) = service.getCurrentPoll()
            if (p == null) notFound = true
            else {
                poll = p
                response = resp
                answers = p.questions.map { q -> AnswerPanel(q) }
            }
            render()
        }
    }

    private fun submit() {
        submitting = true
        render()
        val response = PollResponse(
                responses = answers.map { it.createResponse() }
        )
        GlobalScope.launch {
            service.submitResponse(poll!!.id, response)
        }
    }

    override fun render() {
        val poll = poll
        markup().div {
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
}
