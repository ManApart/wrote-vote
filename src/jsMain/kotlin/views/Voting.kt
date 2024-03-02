package views

import categories
import components.checkBoxComponent
import components.saveAndToast
import dto.Ballot
import dto.Category
import dto.Vote
import el
import getActiveBallots
import getBallot
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

suspend fun TagConsumer<HTMLElement>.listActiveBallots() {
    if (categories.isEmpty()) categories = getCategories().associateBy { it.id }
    val ballots = getActiveBallots()
    div {
        id = "active-ballots"
        table {
            id = "active-ballots-table"
            th { +"Category" }
            th { +"Ballot" }
            th { +"Opened" }
            th { +"Closes At" }
            ballots.forEach { ballot ->
                tr("ballot-row") {
                    td { +(categories[ballot.category]?.name ?: "None") }
                    td { +ballot.name }
                    td { +(ballot.opened ?: "") }
                    td { +(ballot.closed ?: "") }
                    onClickFunction = {
                        activeBallot(ballot)
                    }
                }
            }
        }
    }

    //for each vote, show name and when opened, maybe count votes submitted
    //click into ballot view
}

fun activeBallot(ballot: Ballot) {
    updateUrl(Route.VOTE, ballot.id.toString())
    replaceElement {
        nav()
        ballotView(ballot)
    }
}

suspend fun TagConsumer<HTMLElement>.ballotView(ballot: Ballot) {
    val votes = getVotes(ballot.id!!)
    div("ballot") {
        h2 { +ballot.name }
        p {
            id = "vote-count-display"
            +"You have spent ${votes.sumOf { it.points }}/${ballot.points} votes."
        }
        p { +"Each candidate can receive a max of ${ballot.pointsPerChoice} votes." }

        table {
            th { +"" }
            th { +"Candidate" }
            votes.forEachIndexed { i, vote ->
                tr {
                    td {
                        if (ballot.pointsPerChoice > 1) {
                            numberInput {
                                id = "number-$i"
                                onChangeFunction = {
                                    vote.points = el<HTMLInputElement>("number-$i").value.toIntOrNull() ?: 0
                                    updateVoteCount(ballot, votes)
                                }
                            }
                        } else {
                            checkBoxComponent("checkbox-$i", vote.points == 1) { checked ->
                                if (checked) {
                                    vote.points = 1
                                } else vote.points = 0
                                updateVoteCount(ballot, votes)
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
                if (votes.sumOf { it.points } <= ballot.points) {
                    saveAndToast("Saved Vote",
                        "Failed to save vote",
                        { mainPage() }) {
                        saveVotes(ballot.id, votes)
                    }
                } else {
                    println("Votes exceeded!")
                    //TODO Display error message and results of saved above
                }
            }
        }
    }
}

private fun updateVoteCount(ballot: Ballot, votes: List<Vote>) {
    val voteCount = votes.sumOf { it.points }
    el<HTMLParagraphElement>("vote-count-display").textContent = "You have spent ${voteCount}/${ballot.points} votes."
}