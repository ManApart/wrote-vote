package views

import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.js.a
import org.w3c.dom.HTMLElement

fun TagConsumer<HTMLElement>.nav() {
    div {
        id = "nav"
        a(href = "/login", classes = "button-link") { +"Login" }
        a(href = "#auth", classes = "button-link") { +"Auth" }
        a(href = "", classes = "button-link") { +"Home" }
        //TODO - show these based on user having create permission
        a(href = "#categories", classes = "button-link") { +"Manager Categories" }
        a(href = "#ballots", classes = "button-link") { +"Manage Ballots" }
    }
}