package components.vote

import kui.*
import vote.api.v1.Response

class RangeAnswer(options: List<String>, existing: List<Int>) : AnswerPanelInput() {
    private val sliders: List<RangeSlider>

    init {
        require(options.size == existing.size)
        sliders = options.zip(existing) { option, value -> RangeSlider(option, value) }
    }

    override fun createResponse(): Response {
        return Response.selections(sliders.map { it.value })
    }

    override fun render() {
        markup().div {
            for (slider in sliders) {
                component(slider)
            }
        }
    }

    private class RangeSlider(private val title: String, var value: Int) : Component() {
        private var valueAsDouble: Double
            get() = value.toDouble()
            set(value) {
                this.value = value.toInt()
                render()
            }

        override fun render() {
            markup().div {
                label { +title }
                div(classes("d-flex", "align-items-center")) {
                    span(classes("mx-2")) { +"$value%" }
                    span(classes("flex-grow-1")) {
                        inputRange(classes("form-control-range"), model = ::valueAsDouble)
                    }
                }
            }
        }
    }
}