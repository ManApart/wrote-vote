import dto.Category
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.createElement
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement


val jsonMapper = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}
val client = HttpClient {
    install(ContentNegotiation) {
        json(jsonMapper)
    }
}

var categories = mapOf<Int, Category>()

fun main() {
    window.onload = {
        doRouting()
    }
    window.addEventListener("popstate", { e ->
        doRouting()
    })
}

fun el(id: String) = document.getElementById(id) as HTMLElement
fun <T> el(id: String) = document.getElementById(id) as T

fun replaceElement(id: String = "root", rootClasses: String? = null, newHtml: suspend TagConsumer<HTMLElement>.() -> Unit) {
    val root = el<HTMLElement?>(id)
    if (root != null) {
        val newRoot = document.createElement("div") {
            this.id = id
            rootClasses?.split(" ")?.forEach { this.addClass(it) }
        }
        newRoot.append {
            CoroutineScope(Dispatchers.Default).launch {
                newHtml()
            }
        }
        root.replaceWith(newRoot)
    }
}