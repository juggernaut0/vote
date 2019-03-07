package util

import kui.MarkupBuilder
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
