import database.Ballet
import database.Category
import io.ktor.server.application.*
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

    get("/ballet/{ballet}/{user}/votes") {
        val ballet = call.parameters["ballet"]!!.toInt()
        val user = call.parameters["user"]!!.toInt()

        //Auth that user in session is same as user id asked for
        //If votes doen't exist, look up ballet and create votes
        //Get vote by ballet + user
//        val data = transaction {  Ballet[id].toDto() }
//        call.respond(data)
    }
}