package views

import Route
import replaceElement
import updateUrl

fun mainPage() {
    updateUrl(Route.MAIN)
    replaceElement {
        nav()
        listActiveBallots()
    }
}