package auth

import config
import httpClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*


val redirects = mutableMapOf<String, String>()
val userSessions = mutableMapOf<String, UserSession>()

data class UserSession(
    val userId: Int,
    val state: String,
    val accessToken: String,
    val idToken: String
) : Principal

fun Application.configureAuth(){
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session ->
                if(userSessions[session.state]?.idToken == session.idToken ){
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
                    authorizeUrl = "http://127.0.0.1:4444/oauth2/auth",
                    authorizeUrlInterceptor = {
                                              println("auth")
                    },
                    accessTokenUrl = "http://127.0.0.1:4444/oauth2/token",
                    accessTokenInterceptor = {
                        println("token")
                    },
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
