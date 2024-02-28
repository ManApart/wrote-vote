package views

import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.id
import org.w3c.dom.HTMLElement

fun TagConsumer<HTMLElement>.nav() {
    div {
        id = "nav"
        a(href = "", classes = "button-link") { +"Home" }
        a(href = "#auth", classes = "button-link") { +"Auth" }
    }
}