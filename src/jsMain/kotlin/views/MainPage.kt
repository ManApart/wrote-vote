package views

import replaceElement
import updateUrl

fun mainPage() {
    updateUrl("/")
    replaceElement {
        nav()
        listActiveBallets()
    }
}