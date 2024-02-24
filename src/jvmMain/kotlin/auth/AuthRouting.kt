package auth

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
                    append("client_id", "0358d9a1-7f9b-4843-a227-4f5f116b492b")
                    append("client_secret", "3vWUF-wtquk5tbphLMK49Tbw7r")
                    append("grant_type", "authorization_code")
                    append("state", state!!)
                    append("code", code!!)
                    append("redirect_uri", "http://localhost:8080/callback")
                }
            ))
        }
        val principal: Map<String, String>? = tokenRequest.body()
        if (code != null && state != null && principal != null) {
            val session = UserSession(state, principal["id_token"]!!)
            call.sessions.set(session)
            userSessions[state] = session
            redirects[state]?.let { redirect ->
                call.respondRedirect(redirect)
                return@get
            }
        }
        call.respondRedirect("/home")
    }

    authenticate("auth-session") {
        get("/home") {
            val principal = call.principal<UserSession>()!!
            val userInfo = getPersonalGreeting(httpClient, principal)
            call.respondText("Hello, ${userInfo}! Welcome home!")
        }
    }
}

private suspend fun getPersonalGreeting(
    httpClient: HttpClient,
    userSession: UserSession
): String {

    val request = httpClient.get("http://localhost:4444/userinfo") {
        headers {
            println(userSession.token)
            append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
        }
    }
    return request.bodyAsText()
}