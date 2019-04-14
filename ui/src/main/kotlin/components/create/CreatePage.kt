package components.create

import components.CollapsibleAlert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollToOptions
import services.FetchException
import services.VoteService
import util.labelledTextInput
import vote.api.v1.PollCreateRequest
import kotlin.browser.window

class CreatePage(private val service: VoteService) : Component() {
    private val alert = CollapsibleAlert("create-page-alert")

    private var title: String = ""
    private val questions: MutableList<QuestionPanel> = mutableListOf()

    private var submitting = false

    private fun addQuestion() {
        questions.add(QuestionPanel(this))
        render()
    }

    fun removeQuestion(q: QuestionPanel) {
        questions.remove(q)
        render()
    }

    fun isFirst(q: QuestionPanel): Boolean {
        return questions.indexOf(q) == 0
    }

    fun isLast(q: QuestionPanel): Boolean {
        return questions.indexOf(q) == questions.lastIndex
    }

    fun moveUp(q: QuestionPanel) {
        val i = questions.indexOf(q)
        if (i == 0) return
        swap(i, i-1)
        render()
    }

    fun moveDown(q: QuestionPanel) {
        val i = questions.indexOf(q)
        if (i == questions.lastIndex) return
        swap(i, i+1)
        render()
    }

    private fun swap(a: Int, b: Int) {
        val t = questions[a]
        questions[a] = questions[b]
        questions[b] = t
    }

    private fun createPoll() {
        submitting = true
        render()
        val poll = PollCreateRequest(title, questions.map { it.createQuestion() })
        GlobalScope.launch {
            try {
                service.createPoll(poll)
            } catch (e: FetchException) {
                console.error(e)
                window.scrollTo(ScrollToOptions(0.0, 0.0, ScrollBehavior.SMOOTH))
                if (e.status == 400.toShort()) {
                    alert.show("There were errors publishing the poll: ${e.body}")
                    submitting = false
                    render()
                } else {
                    alert.show("There was an error publishing the poll. (${e.status})")
                }
            }
        }
    }

    override fun render() {
        markup().div {
            h2 { +"Create a Poll" }
            component(alert)
            labelledTextInput("Title", ::title)
            for (q in questions) {
                component(q)
            }
            button(Props(
                    classes = listOf("btn", "btn-primary", "btn-block", "mb-2"),
                    click = { addQuestion() }
            )) {
                +"Add Question"
            }
            button(Props(
                    classes = listOf("btn", "btn-success", "btn-block", "mb-3"),
                    click = { createPoll() },
                    disabled = submitting || questions.isEmpty()
            )) {
                if (submitting) {
                    +"Publishing..."
                } else {
                    +"Publish Poll"
                }
            }
        }
    }
}
