package views

import el
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.createElement
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.style
import org.w3c.dom.HTMLElement

fun mainPage() {
    println("Main page")
    replaceElement {
        nav()
        div("sample") { style = "background-color: var(--blue)" }
        div("sample") { style = "background-color: var(--dark-blue)" }
        div("sample") { style = "background-color: var(--red)" }
        CoroutineScope(Dispatchers.Default).launch {
            listActiveVotes()
        }
    }
}


fun replaceElement(id: String = "root", rootClasses: String? = null, newHtml: TagConsumer<HTMLElement>.() -> Unit) {
    val root = el<HTMLElement?>(id)
    if (root != null) {
        val newRoot = document.createElement("div") {
            this.id = id
            rootClasses?.split(" ")?.forEach { this.addClass(it) }
        }
        newRoot.append {
            newHtml()
        }
        root.replaceWith(newRoot)
    }
}