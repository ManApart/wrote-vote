import auth.UserSession
import database.Ballet
import database.Category
import database.Vote
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.voteApiRoutes() {
    get("/categories") {
        val data = transaction {  Category.all().map { it.toDto() }}
        call.respond(data)
    }

    get("/ballets") {
        //TODO - filter active only per query string
        val data = transaction {  Ballet.all().map { it.toDto() }}
        call.respond(data)
    }

    get("/ballet/{id}") {
        val id = call.parameters["id"]!!.toInt()
        val data = transaction {  Ballet[id].toDto() }
        call.respond(data)
    }

    get("/ballet/{ballet}/votes") {
        val ballet = call.parameters["ballet"]!!.toInt()
        val principal = call.principal<UserSession>()!!

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

        //Auth user matches ballet / votes
        //Get specific ballet
        //Check vote count per vote and total
        //Update votes (only point count)
    }
}