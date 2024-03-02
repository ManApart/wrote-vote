package views

import kotlinx.html.div
import replaceElement
import updateUrl

fun categoriesPage() {
    updateUrl(Route.CATEGORIES)
    replaceElement {
        nav()
        div {
            +"Coming soon"
        }
    }
}