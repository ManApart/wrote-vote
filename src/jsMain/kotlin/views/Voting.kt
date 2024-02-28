package views

import dto.Ballet
import dto.Category
import getActiveBallets
import getBallet
import getCategories
import getVotes
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import replaceElement
import updateUrl

private var categories = mapOf<Int, Category>()

suspend fun TagConsumer<HTMLElement>.listActiveBallets() {
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
                        activeBallet(ballet)
                    }
                }
            }
        }
    }

    //for each vote, show name and when opened, maybe count votes submitted
    //click into ballet view
}

fun activeBallet(ballet: Ballet) {
    updateUrl("ballet", ballet.id.toString())
    replaceElement {
        nav()
        balletView(ballet)
    }
}

suspend fun TagConsumer<HTMLElement>.balletView(ballet: Ballet) {
    //TODO - get user id from session
    val votes = getVotes(ballet.id, 0)
    div("ballet") {
        h2 { +ballet.name }
        //TODO - update per votes used
        p { +"You have ${ballet.points} votes to spend." }
        p { +"Each candidate can receive a max of ${ballet.pointsPerChoice} votes." }

        table {
            th { +"" }
            th { +"Candidate" }
            votes.forEach { vote ->
                tr {
                    td {
                        checkBoxInput {  }
                    }
                    td { +vote.selectionName }
                }
            }
        }
        button(classes = "button-alert") {
            +"Submit"
        }
    }
}

fun TagConsumer<HTMLElement>.createBallet() {

}