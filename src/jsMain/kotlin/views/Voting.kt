package views

import getCategories
import kotlinx.html.TagConsumer
import org.w3c.dom.HTMLElement

suspend fun TagConsumer<HTMLElement>.listActiveVotes() {
    println(getCategories().map { it.name })
    //for each vote, show name and when opened, maybe count votes submitted
    //click into ballet view
}

fun TagConsumer<HTMLElement>.balletView() {

}

fun TagConsumer<HTMLElement>.editVote() {

}