import database.Category
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.voteApiRoutes() {
    get("/categories") {
        val data = transaction {  Category.all().map { it.toDto() }}
        call.respond(data)
    }
}