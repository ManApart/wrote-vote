package components

import el
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement

fun TagConsumer<HTMLElement>.checkBoxComponent(divId: String, initiallyChecked: Boolean, onCheck: (Boolean) -> Unit) {
    var checked: Boolean
    div("checkbox") {
        id = divId
        checked = initiallyChecked
        if (checked) {
            +"X"
        }
        onClickFunction = {
            checked = !checked
            onCheck(checked)
            el(divId).innerHTML = if(checked) "X" else ""
        }
    }
}