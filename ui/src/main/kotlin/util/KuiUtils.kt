package util

import kui.AbstractMarkupBuilder
import kui.classes
import kotlin.reflect.KMutableProperty0

fun AbstractMarkupBuilder.labelledTextInput(label: String, prop: KMutableProperty0<String>) {
    label {
        +label
        inputText(classes("form-control"), prop) // TODO add placeholder when kui is updated
    }
}

fun <T : Any> AbstractMarkupBuilder.labelledDropdown(label: String, prop: KMutableProperty0<T>, options: List<T>) {
    label {
        +label
        select(classes("form-control"), options, prop) // TODO add placeholder when kui is updated
    }
}
