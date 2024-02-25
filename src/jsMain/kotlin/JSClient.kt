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
import views.authPage
import views.mainPage


val client = HttpClient {
    install(ContentNegotiation) {
        json()
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
    doRouting(window.location.hash)
}

fun doRouting(windowHash: String) {
    val section = windowHash.split("/").takeIf { it.size == 2 }?.last()
    section?.let { println("Section: $it") }
    when {
        windowHash.startsWith("#auth") -> {
            authPage()
        }
        else -> mainPage()
    }
    section?.let { el<HTMLElement?>(it)?.scrollIntoView() }
}

fun el(id: String) = document.getElementById(id) as HTMLElement
fun <T> el(id: String) = document.getElementById(id) as T