import auth.UserSession
import database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

fun Route.voteApiRoutes() {
    get("/categories") {
        val data = transaction { Category.all().map { it.toDto() } }
        call.respond(data)
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

        val existingBallet = transaction {  Ballet[ballet] }
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