import auth.Permission
import auth.UserSession
import auth.authedWith
import database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

fun Route.voteApiRoutes() {
    get("/categories") {
        val data = transaction { Category.all().map { it.toDto() } }
        call.respond(data)
    }

    post("/category") {
        authedWith(Permission.CREATE) {
            val category = call.receiveText()
            val id = transaction {
                Categories.insertIgnoreAndGetId { it[name] = category }?.value
            }
            call.respond(HttpStatusCode.Created, id ?: -1)
        }
    }

    get("/category/{category}/candidates") {
        val category = call.receive<Int>()
        val data = transaction { Candidate.find { Candidates.category.eq(category) }.map { it.toDto() } }
        call.respond(data)
    }

    post("candidate") {
        authedWith(Permission.CREATE) {
            val candidate = call.receive<dto.Candidate>()
            val id = transaction { Candidates.insertIgnoreAndGetId { it[name] = candidate.name; it[category] = candidate.categoryId }?.value }
            call.respond(HttpStatusCode.Created, id ?: -1)
        }
    }

    get("/ballots") {
        val data = transaction { Ballot.find { Ballots.closed.isNull() }.map { it.toDto() } }
        call.respond(data)
    }

    get("/ballot/{id}") {
        val id = call.parameters["id"]!!.toInt()
        val data = transaction { Ballot[id].toDto() }
        call.respond(data)
    }

    post("/ballot") {
        authedWith(Permission.CREATE) {
            val b = call.receive<dto.Ballot>()
            val id = transaction {
                Ballots.insertAndGetId {
                    it[name] = b.name
                    it[category] = b.category
                    it[points] = b.points
                    it[pointsPerChoice] = b.pointsPerChoice
                }.value
            }
            call.respond(HttpStatusCode.Created, id)
        }
    }

    //update the ballot
    put("/ballot/{id}") {
        //TODO check permissions, and that it's the author of the ballot
        authedWith(Permission.CREATE) {
            val id = call.parameters["id"]!!.toInt()
            val candidates = call.receive<List<dto.Ballot>>()

            transaction {
                val existingBallot = Ballot[id]
                if (existingBallot.opened == null) {
//TODO - update candidates in different method
                    val existingCandidates = BallotCandidate.find { BallotCandidates.ballot.eq(id) }
                }
            }
            //TODO
            //once ballot is opened, no edits other than closing it
        }
    }

    put("/ballot/{ballot}/candidates") {
        authedWith(Permission.CREATE) {
            val id = call.parameters["id"]!!.toInt()
            val candidates = call.receive<List<dto.Ballot>>()
            //TODO check permissions, and that it's the author of the ballot

            transaction {
                val existingBallot = Ballot[id]
                if (existingBallot.opened == null) {
//TODO - update candidates in different method
                    val existingCandidates = BallotCandidate.find { BallotCandidates.ballot.eq(id) }
                    //Deactivate missing candidates
                    //Add new candidates
                }
            }
            //TODO
            //once ballot is opened, no edits other than closing it
        }
    }

    get("/ballot/{ballot}/votes") {
        authedWith(Permission.VOTE) {
            val ballotId = call.parameters["ballot"]!!.toInt()
            val principal = call.principal<UserSession>()!!

            val votes = transaction {
                addLogger(StdOutSqlLogger)
                Vote.getForBallot(ballotId, principal.userId)
                    .map { it.toDto() }
            }

            if (votes.isNotEmpty()) {
                call.respond(votes)
            } else {
                val newVotes = transaction {
                    BallotCandidate.find { BallotCandidates.ballot.eq(ballotId) }.map { candidate ->
                        Votes.insertAndGetId {
                            it[ballot] = ballotId
                            it[user] = principal.userId
                            it[selection] = candidate.candidate.id
                        }
                    }.map { Vote[it].toDto() }
                }
                println(newVotes)
                call.respond(newVotes)
            }
        }
    }

    put("/ballot/{ballot}/votes") {
        authedWith(Permission.VOTE) {
            val ballot = call.parameters["ballot"]!!.toInt()
            val votes = call.receive<List<dto.Vote>>()
            val principal = call.principal<UserSession>()!!

            val existingBallot = transaction { Ballot[ballot] }
            val totalPoints = votes.sumOf { it.points }
            val maxPointsPerChoice = votes.maxOf { it.points }

            if (totalPoints > existingBallot.points) {
                throw IllegalArgumentException("${principal.userId} Voted for $totalPoints which is greater than ballot's ${existingBallot.points}")
            }
            if (maxPointsPerChoice > existingBallot.pointsPerChoice) {
                throw IllegalArgumentException("Vote from ${principal.userId} has a candidate with $maxPointsPerChoice which is greater than ballot's ${existingBallot.pointsPerChoice}")
            }

            val existingVotes = transaction { Vote.getForBallot(ballot, principal.userId).associateBy { it.id.value } }

            transaction {
                votes.forEach { vote ->
                    val match = existingVotes[vote.id]
                    match?.points = vote.points
                }
            }
            call.respond(HttpStatusCode.Accepted)
        }
    }
}