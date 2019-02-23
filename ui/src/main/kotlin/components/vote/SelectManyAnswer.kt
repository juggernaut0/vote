package components.vote

import components.Checkbox
import vote.api.v1.Response

class SelectManyAnswer(name: String, options: List<String>, selected: Set<Int>) : AnswerPanelInput() {
    private val checks = options.mapIndexed { i, opt -> Checkbox("$name-$i", opt, i in selected) }

    override fun createResponse(): Response {
        return Response.selections(checks.withIndex().mapNotNull { (i, c) -> i.takeIf { c.selected } })
    }

    override fun render() {
        markup().div {
            for (c in checks) {
                component(c)
            }
        }
    }
}
