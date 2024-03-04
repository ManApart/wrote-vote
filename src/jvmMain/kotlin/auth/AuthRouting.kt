package auth

import database.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import jsonMapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

@Serializable
data class AuthToken(val sub: String, val aud: List<String>)

@Serializable
data class JWT(val sub: String, val azp: String, val upn: String, val name: String)

fun Routing.authRoutes() {
    authenticate("auth-keycloak") {
        get("/login") { }

        get("/callback") {
            println("Got Callback")
            val code = call.request.queryParameters["code"]!!
            val state = call.request.queryParameters["state"]
            val scopes = call.request.queryParameters["scope"]
            val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()!!
            println("Details: $code, $state, $scopes, $principal")
            val jwtPayload = principal.accessToken.split(".")[1]
            val decoded = String(Base64.getDecoder().decode(jwtPayload))
            val jwt = jsonMapper.decodeFromString<JWT>(decoded)
//            if (jwt.azp != config.authClientId) throw IllegalStateException("Token is for wrong audience!") //Enable once we're genning keycloak client id + secret
            val user = transaction {
                val userCount = User.find { Users.authId.eq(jwt.upn) }.count()
                if (userCount == 1L) {
                    User.find { Users.authId.eq(jwt.upn) }.single()
                } else {
                    val userDb = User.new { name = jwt.name; authId = jwt.upn }
                    val groupDb = Group.find { Groups.name.eq("Voter") }.single()
                    UserGroups.insertIgnore { it[group] = groupDb.id; it[user] = userDb.id }
                    userDb
                }
            }
            val permissions = transaction { user.getPermissions() }
            val id = user.id.value
            val key = UUID.randomUUID().toString()
            val expires = Instant.now().plus(1, ChronoUnit.DAYS)

            println("User $id logged in with permissions $permissions")

            val serverSession = ServerSideUserSession(id, key, code, principal.accessToken, expires, permissions)
            val session = UserSession(id, key)
            call.sessions.set(session)
            userSessions[id] = serverSession
            call.respondRedirect("/")
        }
    }


    authenticate("auth-keycloak") {
        get("/user-info") {
            val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
//            val userInfo = getPersonalGreeting(httpClient, principal)
//            call.respondText("Hello, ${userInfo}!")
            println("Principal: $principal")
            call.respondText("Hello!")
        }
    }
}

private suspend fun getPersonalGreeting(
    httpClient: HttpClient,
    userSession: UserSession
): String {

    val request = httpClient.get("http://localhost:4444/userinfo") {
        headers {
            val serverSession = userSessions[userSession.userId]!!
            println(serverSession.accessToken)
            append(HttpHeaders.Authorization, "Bearer ${serverSession.accessToken}")
        }
    }
    return request.bodyAsText()
}