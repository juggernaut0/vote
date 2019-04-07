package util

import kui.MarkupBuilder
import kui.Props
import kui.classes
import kotlin.reflect.KMutableProperty0

fun MarkupBuilder.labelledTextInput(label: String, prop: KMutableProperty0<String>) {
    label {
        +label
        inputText(classes("form-control"), model = prop)
    }
}

fun <T : Any> MarkupBuilder.labelledDropdown(label: String, prop: KMutableProperty0<T>, options: List<T>) {
    label {
        +label
        select(classes("form-control"), options, prop)
    }
}

fun MarkupBuilder.modal(id: String, title: String, style: String = "primary", ok: (() -> Unit)? = null, inner: MarkupBuilder.() -> Unit) {
    div(Props(id = id, classes = listOf("modal", "fade"))) {
        div(classes("modal-dialog")) {
            div(classes("modal-content")) {
                div(classes("modal-header")) {
                    h5(classes("modal-title")) { +title }
                    button(Props(
                            classes = listOf("close"),
                            attrs = mapOf("data-dismiss" to "modal")
                    )) { +X }
                }
                div(classes("modal-body")) {
                    inner()
                }
                div(classes("modal-footer")) {
                    button(Props(
                            classes = listOf("btn", "btn-secondary"),
                            attrs = mapOf("data-dismiss" to "modal")
                    )) { +"Close" }
                    if (ok != null) {
                        button(Props(
                                classes = listOf("btn", "btn-$style"),
                                attrs = mapOf("data-dismiss" to "modal"),
                                click = ok
                        )) { +"Ok" }
                    }
                }
            }
        }
    }
}
