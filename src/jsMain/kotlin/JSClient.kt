import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import kotlinx.html.js.a
import kotlinx.html.js.div
import kotlinx.html.style
import org.w3c.dom.HTMLElement


val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

fun main() {
    window.onload = {
        el("root").append {
            div("sample") { style = "background-color: var(--blue)" }
            div("sample") { style = "background-color: var(--dark-blue)" }
            div("sample") { style = "background-color: var(--red)" }
            a(href = "/login") { +"Login" }
        }
        CoroutineScope(Dispatchers.Default).launch {

        }
    }
}


fun el(id: String) = document.getElementById(id) as HTMLElement
fun <T> el(id: String) = document.getElementById(id) as T