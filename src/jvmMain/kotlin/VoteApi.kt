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
}