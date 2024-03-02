package database

import auth.Permission
import config
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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
        SchemaUtils.create(UserGroups)
        SchemaUtils.create(Groups)
        SchemaUtils.create(GroupRoles)
        SchemaUtils.create(Roles)
        exec(buildString {
            append("DO \$\$ BEGIN ")
            append("IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'permission') THEN ")
            append("CREATE TYPE Permission AS ENUM ('VIEW', 'VOTE', 'CREATE'); ")
            append("END IF;" )
            append("END \$\$ ")
        })
        SchemaUtils.create(RolePermissions)
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

            val userDb = User.new { name = "Bob"; sub = "foo@bar.com" }
            Vote.new { this.ballet = ballet; this.user = userDb; selection = bc1 }

            val groupId = Groups.insertAndGetId { it[name] = "Voter" }
            val roleId = Roles.insertAndGetId { it[name] = "Vote" }
            UserGroups.insertIgnore { it[user] = userDb.id; it[group] = groupId }
            GroupRoles.insertIgnore { it[group] = groupId; it[role] = roleId }
            RolePermissions.insertIgnore { it[role] = roleId; it[permission] = Permission.VOTE }
            RolePermissions.insertIgnore { it[role] = roleId; it[permission] = Permission.CREATE }

        }
    }
}
