package views

import kotlinx.html.div
import replaceElement
import updateUrl

fun manageBallotsPage() {
    updateUrl(Route.BALLOT)
    replaceElement {
        nav()
        div {
            +"Coming soon"
        }
    }
}