import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

private lateinit var database: Database


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
//    Database.connect("jdbc:sqlite:/data/data.db", "org.sqlite.JDBC")
    database = Database.connect("jdbc:sqlite:file:test?mode=memory&cache=shared", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel =
        Connection.TRANSACTION_SERIALIZABLE
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
//        println("Users: ${User.all().map { it.name }}")
    }
}