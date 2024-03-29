package views

import kotlinx.html.js.a
import replaceElement

fun authPage() {
    replaceElement {
        a(href = "", classes = "button-link") { +"Home" }
        a(href = "", classes = "button-link") { +"Register" }
        a(href = "/user-info", classes = "button-link") { +"User Info" }
        a(href = "/login", classes = "button-link") { +"Login" }
        a(href = "/login-oauth", classes = "button-link") { +"Login Oauth" }
        a(href = "/logout", classes = "button-link button-alert") { +"Log Out" }
    }
}
