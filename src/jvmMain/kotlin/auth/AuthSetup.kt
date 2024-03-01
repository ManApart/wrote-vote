package auth

import config
import httpClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*


val redirects = mutableMapOf<String, String>()
//TODO - make these expire
val userSessions = mutableMapOf<Int, ServerSideUserSession>()

data class ServerSideUserSession(
    val userId: Int,
    val key: String,
    val idToken: String,
    val accessToken: String,
)

data class UserSession(
    val userId: Int,
    val key: String,
) : Principal

fun Application.configureAuth(){
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session ->
                if(userSessions[session.userId]?.key == session.key ){
                    session
                } else null
            }
            challenge {
                call.respondRedirect("/login")
            }
        }

        oauth("auth-oauth-hydra") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "hydra",
                    authorizeUrl = "http://localhost:4444/oauth2/auth",
                    accessTokenUrl = "http://localhost:4444/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = config.authClientId,
                    clientSecret = config.authClientSecret,
                    defaultScopes = listOf("offline", "openid", "profile"),
                    extraAuthParameters = listOf("access_type" to "offline"),
                    passParamsInURL = true,
                    onStateCreated = { call, state ->
                        call.request.queryParameters["redirectUrl"]?.let {
                            println("Adding redirect: $state $it")
                            redirects[state] = it
                        }
                    }
                )
            }
            client = httpClient
        }
    }
}
