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
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

fun Route.voteApiRoutes() {
    get("/categories") {
        val data = transaction { Category.all().map { it.toDto() } }
        call.respond(data)
    }

    post("category") {
        authedWith(Permission.CREATE) {
            val category = call.receiveText()
            transaction {
                Categories.insertIgnore { it[name] = category }
            }
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
            call.respond(id ?: -1)
        }
    }

    get("/ballets") {
        val data = transaction { Ballet.find { Ballets.closed.isNull() }.map { it.toDto() } }
        call.respond(data)
    }

    get("/ballet/{id}") {
        val id = call.parameters["id"]!!.toInt()
        val data = transaction { Ballet[id].toDto() }
        call.respond(data)
    }

    post("/ballet") {
        //TODO -should this take candidates as well?
        authedWith(Permission.CREATE) {
            val b = call.receive<dto.Ballet>()
            val id = transaction {
                Ballets.insertAndGetId {
                    it[name] = b.name
                    it[category] = b.category
                    it[points] = b.points
                    it[pointsPerChoice] = b.pointsPerChoice
                }.value
            }
            call.respond(id)
        }
    }

    //update the ballet
    put("/ballet/{id}") {
        //TODO check permissions, and that it's the author of the ballet
        authedWith(Permission.CREATE) {
            val id = call.parameters["id"]!!.toInt()
            val candidates = call.receive<List<dto.Ballet>>()

            transaction {
                val existingBallet = Ballet[id]
                if (existingBallet.opened == null) {
                    val existingCandidates = BalletCandidate.find { BalletCandidates.ballet.eq(id) }
//TODO - update candidates
                }
            }
            //TODO
            //once ballet is opened, no edits other than closing it
        }
    }

    get("/ballet/{ballet}/votes") {
        val balletId = call.parameters["ballet"]!!.toInt()
        val principal = call.principal<UserSession>()!!

        val votes = transaction {
            Vote.getForBallet(balletId, principal.userId)
        }.map { it.toDto() }

        if (votes.isNotEmpty()) {
            call.respond(votes)
        } else {
            val newVotes = transaction {
                BalletCandidate.find { BalletCandidates.ballet.eq(balletId) }.map { candidate ->
                    Votes.insertAndGetId {
                        it[ballet] = balletId
                        it[user] = principal.userId
                        it[selection] = candidate.candidate.id
                    }
                }.map { Vote[it].toDto() }
            }
            println(newVotes)
            call.respond(newVotes)
        }
    }

    put("/ballet/{ballet}/votes") {
        val ballet = call.parameters["ballet"]!!.toInt()
        val votes = call.receive<List<dto.Vote>>()
        val principal = call.principal<UserSession>()!!

        val existingBallet = transaction { Ballet[ballet] }
        val totalPoints = votes.sumOf { it.points }
        val maxPointsPerChoice = votes.maxOf { it.points }

        if (totalPoints > existingBallet.points) {
            throw IllegalArgumentException("${principal.userId} Voted for $totalPoints which is greater than ballet's ${existingBallet.points}")
        }
        if (maxPointsPerChoice > existingBallet.pointsPerChoice) {
            throw IllegalArgumentException("Vote from ${principal.userId} has a candidate with $maxPointsPerChoice which is greater than ballet's ${existingBallet.pointsPerChoice}")
        }

        val existingVotes = transaction { Vote.getForBallet(ballet, principal.userId).associateBy { it.id.value } }

        transaction {
            votes.forEach { vote ->
                val match = existingVotes[vote.id]
                match?.points = vote.points
            }
        }
        call.respond(HttpStatusCode.Accepted)
    }
}