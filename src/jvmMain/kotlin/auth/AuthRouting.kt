package auth

import config
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.util.Base64

fun Routing.authRoutes() {
    authenticate("auth-oauth-hydra") {
        get("/login") { }
    }
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
            val map = Json.parseToJsonElement(decoded)
            val sub = map.jsonObject.getValue("sub")
            //TODO translate sub to user id
            val session = UserSession(0, state, principal["access_token"]!!, principal["id_token"]!!)
            call.sessions.set(session)
            userSessions[state] = session
            redirects[state]?.let { redirect ->
                call.respondRedirect(redirect)
                return@get
            }
        }
        call.respondRedirect("/user-info")
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
            println(userSession.idToken)
            append(HttpHeaders.Authorization, "Bearer ${userSession.accessToken}")
        }
    }
    return request.bodyAsText()
}