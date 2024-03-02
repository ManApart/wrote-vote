package auth

import config
import database.User
import database.Users
import httpClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalStateException
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import java.util.Base64
import java.util.UUID

@Serializable
data class AuthToken(val sub: String, val aud: List<String>)

fun Routing.authRoutes() {
    authenticate("auth-oauth-hydra") {
        get("/login") { }
    }
    put("/login") { call.respondRedirect("/login") }
    get("/callback") {
        val code = call.request.queryParameters["code"]
        val state = call.request.queryParameters["state"]
        val scopes = call.request.queryParameters["scope"]

        val tokenRequest = httpClient.post("http://127.0.0.1:4444/oauth2/token") {
            setBody(FormDataContent(
                Parameters.build {
                    append("client_id", config.authClientId)
                    append("client_secret", config.authClientSecret)
                    append("grant_type", "authorization_code")
                    append("state", state!!)
                    append("code", code!!)
                    append("scope", scopes!!)
                    append("redirect_uri", "http://localhost:8080/callback")
                }
            ))
        }
        val principal: Map<String, String>? = tokenRequest.body()
        if (code != null && state != null && principal != null) {
            val jwtPayload = principal["id_token"]!!.split(".")[1]
            val decoded = String(Base64.getDecoder().decode(jwtPayload))
            val jwt = jsonMapper.decodeFromString<AuthToken>(decoded)
            if (!jwt.aud.contains(config.authClientId)) throw IllegalStateException("Token is for wrong audience!")
            val user = transaction {
                User.find { Users.sub.eq(jwt.sub) }.single()
            }
            val permissions = transaction { user.getPermissions() }
            val id = user.id.value
            val key = UUID.randomUUID().toString()
            val expires = Instant.now().plus(1, ChronoUnit.DAYS)

            println("User $id logged in with permissions $permissions")

            val serverSession = ServerSideUserSession(id, key, principal["id_token"]!!, principal["access_token"]!!, expires, permissions)
            val session = UserSession(id, key)
            call.sessions.set(session)
            userSessions[id] = serverSession
            redirects[state]?.let { redirect ->
                call.respondRedirect(redirect)
                return@get
            }
        }
        call.respondRedirect("/")
    }

    authenticate("auth-session") {
        get("/user-info") {
            val principal = call.principal<UserSession>()!!
            val userInfo = getPersonalGreeting(httpClient, principal)
            call.respondText("Hello, ${userInfo}!")
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
            println(serverSession.idToken)
            append(HttpHeaders.Authorization, "Bearer ${serverSession.accessToken}")
        }
    }
    return request.bodyAsText()
}