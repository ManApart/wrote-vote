import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import views.authPage
import views.mainPage
import views.manageBallotsPage

enum class Route(val path: String, val route: suspend (String?) -> Unit) {
    MAIN("/", { mainPage() }),
    AUTH("#auth", { authPage() }),
    BALLOT("#ballot", { manageBallotsPage() }),
    CATEGORIES("#categories", { views.categoriesPage() }),
    VOTE("#vote/", { section ->
        val ballot = getBallot(section?.toIntOrNull() ?: 0)
        views.activeBallot(ballot)
    })
    ;

    fun isHere(windowHash: String) = windowHash.startsWith(path)
}

fun doRouting() {
    CoroutineScope(Dispatchers.Default).launch {
        doRouting(window.location.hash)
    }
}

suspend fun doRouting(windowHash: String) {
    val section = windowHash.split("/").takeIf { it.size == 2 }?.last()
    section?.let { println("Section: $it") }
    val route = Route.entries.firstOrNull { it.isHere(windowHash) }
    if (route != null) route.route(section) else mainPage()
    section?.let { el<HTMLElement?>(it)?.scrollIntoView() }
}

fun updateUrl(route: Route, section: String? = null) {
    val pathName = route.path.split("/").first().capitalize()
    val newPath = route.path + (section?.let { "/$it" } ?: "")
    if (!window.location.href.endsWith(newPath)) {
        window.history.pushState(null, "", newPath)
    }
    if (pathName.isBlank()) {
        document.title = "Vote"
    } else {
        document.title = "Vote: $pathName"
    }
}
