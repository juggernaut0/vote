package components.create

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import services.VoteService
import util.labelledTextInput
import vote.api.v1.PollCreateRequest

class CreatePage(private val service: VoteService) : Component() {
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
            service.createPoll(poll)
        }
    }

    override fun render() {
        markup().div {
            h2 { +"Create a Poll" }
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
