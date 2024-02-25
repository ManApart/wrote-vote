package database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Categories : IntIdTable() {
    val name = varchar("name", 50)
}

class Category(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Category>(Categories)
    var name by Categories.name

    fun toDto(): dto.Category{
        return dto.Category(name)
    }
}

object Candidates : IntIdTable() {
    val name = varchar("name", 100)
    val category = reference("category", Categories.id)
}

class Candidate(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Candidate>(Candidates)

    var name by Candidates.name
    var category by Category referencedOn Candidates.category
}

object Ballets : IntIdTable() {
    val name = varchar("name", 100)
    val category = reference("category", Categories.id)
    val points = integer("points").default(1)
    val opened = datetime("opened").nullable()
    val closed = datetime("closed").nullable()
}

class Ballet(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Ballet>(Ballets)

    var name by Ballets.name
    var category by Category referencedOn Ballets.category
    var points by Ballets.points
    var opened by Ballets.opened
    var closed by Ballets.closed
}

object BalletCandidates : IntIdTable() {
    val ballet = reference("ballet", Ballets.id)
    val candidate = reference("candidate", Candidates.id)
}

class BalletCandidate(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BalletCandidate>(BalletCandidates)

    var ballet by Ballet referencedOn BalletCandidates.ballet
    var candidate by Candidate referencedOn BalletCandidates.candidate
}


object Votes : IntIdTable() {
    val ballet = reference("ballet", Ballets.id)
    val selection = reference("selection", BalletCandidates.id)
    val user = reference("user", Users.id)
    val points = integer("points").default(1)
    val submitted = datetime("date_created").clientDefault { LocalDateTime.now() }
    val revoked = bool("revoked").nullable()
}

class Vote(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Vote>(Votes)

    var ballet by Ballet referencedOn Votes.ballet
    var user by User referencedOn Votes.user
    var selection by BalletCandidate referencedOn Votes.selection
    var points by Votes.points
    var submitted by Votes.submitted
    var revoked by Votes.revoked
}

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var name by Users.name
}