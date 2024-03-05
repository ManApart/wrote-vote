package auth

import config
import database.*
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
import kotlinx.serialization.decodeFromString
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

fun Routing.authRoutes() {
    authenticate("auth-keycloak") {
        get("/login") { }
    }
    authenticate("auth-oauth-hydra") {
        get("/login-oauth") { }
    }

    get("/callback") {
        val code = call.request.queryParameters["code"]!!
        val principal = call.getPrincipal( "http://localhost:4446/realms/voting/protocol/openid-connect/token", "http://localhost:8080/callback", "wrote-vote", config.keycloakAuthClientSecret)
        val jwt = principal.getJWT<KeycloakJWT>()
        jwt.validate("wrote-vote")
        call.logInUser(jwt.sub, jwt.name, code, principal.accessToken)
    }

    get("/callback-oauth") {
        val principal = call.getPrincipal( "http://127.0.0.1:4444/oauth2/token", "http://localhost:8080/callback-oauth", config.authClientId, config.authClientSecret)
        val jwt = principal.getJWT<HydraJWT>()
        jwt.validate(config.authClientId)
        call.logInUser(jwt.sub, jwt.sub, principal.idToken!!, principal.accessToken)
    }

    authenticate("auth-session") {
        get("/logout") {
            val session = call.principal<UserSession>()!!
            userSessions.remove(session.userId)
            call.respondRedirect("http://localhost:4446/realms/voting/protocol/openid-connect/logout")
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

private suspend fun ApplicationCall.getPrincipal(tokenUrl: String, redirectUri: String, clientId: String, clientSecret: String): JWTWrapper {
    val code = request.queryParameters["code"]!!
    val state = request.queryParameters["state"]
    val scopes = request.queryParameters["scope"]
    return httpClient.post(tokenUrl) {
        setBody(FormDataContent(
            Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("grant_type", "authorization_code")
                append("state", state!!)
                append("code", code)
                scopes?.let { append("scope", it) }
                append("redirect_uri", redirectUri)
            }
        ))
    }.body<JWTWrapper>()
}

private inline fun <reified JWT> JWTWrapper.getJWT(): JWT {
    val jwtPayload = (idToken ?: accessToken).split(".")[1]
    val decoded = String(Base64.getDecoder().decode(jwtPayload))
    return jsonMapper.decodeFromString(decoded)
}

private suspend fun ApplicationCall.logInUser(jwtSub: String, jwtName: String, code: String, accessToken: String){
    val user = transaction {
        val userCount = User.find { Users.authId.eq(jwtSub) }.count()
        if (userCount == 1L) {
            User.find { Users.authId.eq(jwtSub) }.single()
        } else {
            val userDb = User.new { name = jwtName; authId = jwtSub }
            val groupDb = Group.find { Groups.name.eq("Voter") }.single()
            UserGroups.insertIgnore { it[group] = groupDb.id; it[user] = userDb.id }

            userDb
        }
    }
    val permissions = transaction { user.getPermissions() }
    val id = user.id.value
    val key = UUID.randomUUID().toString()
    val expires = Instant.now().plus(1, ChronoUnit.DAYS)

    println("User $id (${user.name}) logged in with permissions $permissions")
    val serverSession = ServerSideUserSession(id, key, code, accessToken, expires, permissions)
    val session = UserSession(id, key)
    this.sessions.set(session)
    userSessions[id] = serverSession
//    redirects[state]?.let { redirect ->
//        call.respondRedirect(redirect)
//        return@get
//    }
    respondRedirect("/")
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