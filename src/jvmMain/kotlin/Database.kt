import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Categories : IntIdTable() {
    val name = varchar("name", 50)
}

object Candidates : IntIdTable() {
    val name = varchar("name", 50)
    val category = reference("category", Categories.id)
}

object Ballets : IntIdTable() {
    val name = varchar("name", 50)
    val category = reference("category", Categories.id)
    val opened = datetime("opened")
    val closed = datetime("closed")
}

object Vote : IntIdTable() {
    val candidate = reference("candidate", Candidates.id)
    val ballet = reference("ballet", Ballets.id)
    val user = reference("user", Users.id)
    val points = integer("points")
    val submitted = datetime("date_created").clientDefault { LocalDateTime.now() }
    val revoked = bool("revoked")
}

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val age = integer("age")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var age by Users.age
}

fun initializeDB() {
    Database.connect(
        "jdbc:postgresql://localhost:15432/postgres", driver = "org.postgresql.Driver",
        user = "root", password = "secret"
    )
}

fun writeTest() {
    println("Writing")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
        User.new { name = "Bob"; age = 10 }
    }
}

fun readTest() {
    println("Reading:")
    transaction {
        println("Users: ${User.all().map { it.name }}")
    }
}