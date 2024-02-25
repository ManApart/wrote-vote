package views

import kotlinx.html.TagConsumer
import kotlinx.html.a
import org.w3c.dom.HTMLElement

fun TagConsumer<HTMLElement>.nav() {
    a(href = "#auth"){+"Auth"}
}