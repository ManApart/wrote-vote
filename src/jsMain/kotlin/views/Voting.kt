package views

import dto.Category
import getActiveBallets
import getCategories
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement

private var categories = mapOf<Int, Category>()

suspend fun TagConsumer<HTMLElement>.listActiveVotes() {
    if (categories.isEmpty()) categories = getCategories().associateBy { it.id }
    val ballets = getActiveBallets()
    div {
        id = "active-ballets"
        table {
            id = "active-ballets-table"
            th { +"Category" }
            th { +"Ballet" }
            th { +"Opened" }
            th { +"Closes At" }
            ballets.forEach { ballet ->
                tr("ballet-row") {
                    td { +(categories[ballet.category]?.name ?: "None") }
                    td { +ballet.name }
                    td { +(ballet.opened ?: "") }
                    td { +(ballet.closed ?: "") }
                    onClickFunction = {
                        println("Go to ballet page for ${ballet.name}")
                    }
                }
            }
        }
    }

    //for each vote, show name and when opened, maybe count votes submitted
    //click into ballet view
}

fun TagConsumer<HTMLElement>.balletView() {

}

fun TagConsumer<HTMLElement>.editVote() {

}