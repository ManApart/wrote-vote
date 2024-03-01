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
import kotlinx.html.js.a
import kotlinx.html.js.div
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import views.activeBallet
import views.authPage
import views.mainPage


val jsonMapper = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}
val client = HttpClient {
    install(ContentNegotiation) {
        json(jsonMapper)
    }
}


fun main() {
    window.onload = {
        doRouting()
    }
    window.addEventListener("popstate", { e ->
        doRouting()
    })
}


fun doRouting() {
    CoroutineScope(Dispatchers.Default).launch {
        doRouting(window.location.hash)
    }
}

suspend fun doRouting(windowHash: String) {
    val section = windowHash.split("/").takeIf { it.size == 2 }?.last()
    section?.let { println("Section: $it") }
    when {
        windowHash.startsWith("#auth") -> {
            authPage()
        }

        windowHash.startsWith("#ballet/") -> {
            val ballet = getBallet(section?.toIntOrNull() ?: 0)
            activeBallet(ballet)
        }

        else -> mainPage()
    }
    section?.let { el<HTMLElement?>(it)?.scrollIntoView() }
}

fun updateUrl(path: String, section: String? = null) {
    val pathName = path.split("/").first().capitalize()
    val newPath = path + (section?.let { "/$it" } ?: "")
    if (!window.location.href.endsWith("#$newPath")) {
        window.history.pushState(null, "", "#$newPath")
    }
    document.title = "Vote: $pathName"
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