package components

import createCategory
import el
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

fun saveAndToast(successMessage: String, failureMessage: String, onSuccess: () -> Unit, call: suspend () -> HttpStatusCode){
    CoroutineScope(Dispatchers.Default).launch {
        val saved = call()
        if (saved == HttpStatusCode.Accepted || saved == HttpStatusCode.Created) {
            println(successMessage)
            //TODO Toast, then redirect
            onSuccess()
        } else {
            println(failureMessage)
        }
    }
}