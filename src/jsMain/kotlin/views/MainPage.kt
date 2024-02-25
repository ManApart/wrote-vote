package views

import replaceElement

fun mainPage() {
    println("Main page")
    replaceElement {
        nav()
        listActiveVotes()
    }
}