package views

import kotlinx.html.js.a

fun authPage() {
    replaceElement {
        a(href = "", classes = "button-link") { +"Back" }
        a(href = "", classes = "button-link") { +"Register" }
        a(href = "/login", classes = "button-link") { +"Login" }
        a(href = "") { +"Log Out" }
    }
}
