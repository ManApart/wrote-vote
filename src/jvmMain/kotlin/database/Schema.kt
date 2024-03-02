package database

import auth.Permission
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.postgresql.util.PGobject

object Categories : IntIdTable() {
    val name = varchar("name", 50)
}

class Category(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Category>(Categories)

    var name by Categories.name

    fun toDto() = dto.Category(id.value, name)
}

object Candidates : IntIdTable() {
    val name = varchar("name", 100)
    val category = reference("category", Categories.id)
}

class Candidate(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Candidate>(Candidates)

    var name by Candidates.name
    var category by Category referencedOn Candidates.category

    fun toDto() = dto.Candidate(name, category.id.value, id.value)
}

object Ballets : IntIdTable() {
    val name = varchar("name", 100)
    val category = reference("category", Categories.id)
    val points = integer("points").default(1)
    val pointsPerChoice = integer("points_per_choice").default(1)
    val opened = datetime("opened").nullable()
    val closed = datetime("closed").nullable()
}

//Add Created by
class Ballet(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Ballet>(Ballets)

    var name by Ballets.name
    var category by Category referencedOn Ballets.category
    var points by Ballets.points
    var pointsPerChoice by Ballets.pointsPerChoice
    var opened by Ballets.opened
    var closed by Ballets.closed

    fun toDto() = dto.Ballet(name, category.id.value, points, pointsPerChoice, opened?.toString(), closed?.toString(), id.value)
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
    val submitted = datetime("date_created").nullable()
}

class Vote(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Vote>(Votes) {
        fun getForBallet(ballet: Int, user: Int) = Vote.find { Votes.ballet.eq(ballet).and { Votes.user.eq(user) } }
    }

    var ballet by Ballet referencedOn Votes.ballet
    var user by User referencedOn Votes.user
    var selection by BalletCandidate referencedOn Votes.selection
    var points by Votes.points
    var submitted by Votes.submitted

    fun toDto() = dto.Vote(id.value, selection.candidate.name, points)
}

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val sub = varchar("sub", 50).index()
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var sub by Users.sub

    fun getPermissions(): List<Permission> {
        return Users.join(UserGroups, JoinType.INNER, Users.id, UserGroups.user)
            .join(GroupRoles, JoinType.INNER, UserGroups.id, GroupRoles.group)
            .join(RolePermissions, JoinType.INNER, GroupRoles.role, RolePermissions.role)
            .select (Users.id.eq(this@User.id))
            .map { it[RolePermissions.permission] }
    }
}

object UserGroups : IntIdTable() {
    val user = reference("user", Users.id)
    val group = reference("group", Groups.id)
}

object Groups : IntIdTable() {
    val name = varchar("name", 50).index()
}

object GroupRoles : IntIdTable() {
    val group = reference("group", Groups.id)
    val role = reference("role", Roles.id)
}

object Roles : IntIdTable() {
    val name = varchar("name", 50).index()
}

object RolePermissions : IntIdTable() {
    val role = reference("role", Roles.id)
    val permission = customEnumeration("enumColumn", "Permission", {value -> Permission.valueOf(value as String)}, { PGEnum("Permission", it) })
}

class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}