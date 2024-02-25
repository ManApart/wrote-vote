package database

import config
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun initializeDB() {
    Database.connect(
        "jdbc:postgresql://localhost:15432/postgres", driver = "org.postgresql.Driver",
        user = config.postgresUser, password = config.postgresPassword
    )
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Categories)
        SchemaUtils.create(Candidates)
        SchemaUtils.create(Ballets)
        SchemaUtils.create(BalletCandidates)
        SchemaUtils.create(Users)
        SchemaUtils.create(Votes)
    }
}

fun seedSampleData() {
    transaction {
        if (User.all().empty()) {
            val lunch = Category.new { name = "lunch" }
            val c1 = Candidate.new { name = "Chick Fila"; category = lunch }
            val c2 = Candidate.new { name = "Chipotle"; category = lunch }
            val ballet = Ballet.new { name = "Sample Vote"; category = lunch }
            val bc1 = BalletCandidate.new { this.ballet = ballet; candidate = c1 }
            BalletCandidate.new { this.ballet = ballet; candidate = c2 }

            val user = User.new { name = "Bob" }
            Vote.new { this.ballet = ballet; this.user = user; selection = bc1 }
        }
    }
}

fun readTest() {
    println("Reading:")
    transaction {
        println("Users: ${User.all().map { it.name }}")
    }
}