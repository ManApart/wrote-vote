package views

import dto.Ballet
import dto.Category
import dto.Vote
import el
import getActiveBallets
import getBallet
import getCategories
import getVotes
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import replaceElement
import saveVotes
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
    val votes = getVotes(ballet.id)
    div("ballet") {
        h2 { +ballet.name }
        p {
            id = "vote-count-display"
            +"You have spent ${votes.sumOf { it.points }}/${ballet.points} votes."
        }
        p { +"Each candidate can receive a max of ${ballet.pointsPerChoice} votes." }

        table {
            th { +"" }
            th { +"Candidate" }
            votes.forEachIndexed { i, vote ->
                tr {
                    td {
                        if (ballet.pointsPerChoice > 1) {
                            numberInput {
                                id = "number-$i"
                                onChangeFunction = {
                                    vote.points = el<HTMLInputElement>("number-$i").value.toIntOrNull() ?: 0
                                    updateVoteCount(ballet, votes)
                                }
                            }
                        } else {
                            checkBoxInput {
                                id = "checkbox-$i"
                                onChangeFunction = {
                                    if (el<HTMLInputElement>("checkbox-$i").checked) {
                                        vote.points = 1
                                    } else vote.points = 0
                                    updateVoteCount(ballet, votes)
                                }
                            }
                        }
                    }
                    td { +vote.selectionName }
                }
            }
        }
        button(classes = "button-alert") {
            +"Submit"
            onClickFunction = {
                if (votes.sumOf { it.points } <= ballet.points){
                    CoroutineScope(Dispatchers.Default).launch {
                        val saved = saveVotes(ballet.id, votes)
                        if (saved == HttpStatusCode.Accepted){
                            println("saved")
                        } else {
                            println("Failed to save")
                        }
                    }
                } else {
                    println("Votes exceeded!")
                    //TODO Display error message and results of saved above
                }
            }
        }
    }
}

private fun updateVoteCount(ballet: Ballet, votes: List<Vote>) {
    val voteCount = votes.sumOf { it.points }
    el<HTMLParagraphElement>("vote-count-display").textContent = "You have spent ${voteCount}/${ballet.points} votes."
}

fun TagConsumer<HTMLElement>.createBallet() {

}