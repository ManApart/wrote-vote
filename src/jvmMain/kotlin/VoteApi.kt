import auth.UserSession
import database.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

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
            Vote.find { Votes.ballet.eq(balletId).and { Votes.user.eq(principal.userId) } }
        }

        if (votes.empty()){
            //TODO - create votes
            transaction {
                val ballet = Ballet[balletId]
                val user = User[principal.userId]
                BalletCandidate.find { BalletCandidates.ballet.eq(balletId) }.forEach { candidate ->
                    //TODO
//                    Vote.new { this.ballet = ballet;  this.user = user}
//                    Votes.insertAndGetId { it[ballet] = 0 }
                }
            }
        } else {
            call.respond(votes.map { it.toDto() })
        }

        //Auth that user in session is same as user id asked for
        //If votes doen't exist, look up ballet and create votes
        //Get vote by ballet + user
//        val data = transaction {  Ballet[id].toDto() }
//        call.respond(data)
    }

    put("/ballet/{ballet}/votes") {
        val ballet = call.parameters["ballet"]!!.toInt()
        val votes = call.receive<List<Vote>>()
        val principal = call.principal<UserSession>()!!

        //Auth user matches ballet / votes, votes match ballet
        //Get specific ballet
        //Check vote count per vote and total
        //Update votes (only point count)
    }
}