package auth

import config
import httpClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import java.lang.IllegalStateException
import java.time.Instant
import java.time.LocalDateTime


val redirects = mutableMapOf<String, String>()

val userSessions = mutableMapOf<Int, ServerSideUserSession>()

enum class Permission { VIEW, VOTE, CREATE }

data class ServerSideUserSession(
    val userId: Int,
    val key: String,
    val authCode: String,
    val accessToken: String,
    val expires: Instant,
    val permissions: List<Permission>,
)

data class UserSession(
    val userId: Int,
    val key: String,
) : Principal

fun Application.configureAuth() {
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session ->
                val serverSession = userSessions[session.userId]
                if (serverSession != null && serverSession.key == session.key && serverSession.expires.isAfter(Instant.now())) {
                    session
                } else null
            }
            //This call for some reason breaks and they have to manually click login. Seems to have extra, unwanted headers
            challenge("/login")
        }

        oauth("auth-keycloak") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "keycloak",
                    authorizeUrl = "http://localhost:4446/realms/voting/protocol/openid-connect/auth",
                    accessTokenUrl = "http://localhost:4446/realms/voting/protocol/openid-connect/token",
                    requestMethod = HttpMethod.Post,
                    clientId = "wrote-vote",
                    clientSecret = config.keycloakAuthClientSecret,
                    accessTokenRequiresBasicAuth = false,
                    defaultScopes = listOf("roles"),
                )
            }
            client = httpClient
        }
//        oauth("auth-oauth-hydra") {
//            urlProvider = { "http://localhost:8080/callback" }
//            providerLookup = {
//                OAuthServerSettings.OAuth2ServerSettings(
//                    name = "hydra",
//                    authorizeUrl = "http://localhost:4444/oauth2/auth",
//                    accessTokenUrl = "http://localhost:4444/oauth2/token",
//                    requestMethod = HttpMethod.Post,
//                    clientId = config.authClientId,
//                    clientSecret = config.authClientSecret,
//                    defaultScopes = listOf("offline", "openid", "profile"),
//                    extraAuthParameters = listOf("access_type" to "offline"),
//                    passParamsInURL = true,
//                    onStateCreated = { call, state ->
//                        call.request.queryParameters["redirectUrl"]?.let {
//                            println("Adding redirect: $state $it")
//                            redirects[state] = it
//                        }
//                    }
//                )
//            }
//            client = httpClient
//        }
    }
}


suspend fun PipelineContext<Unit, ApplicationCall>.authedWith(vararg permission: Permission, block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit) {
    val session = userSessions[call.principal<UserSession>()!!.userId]!!
    if (session.permissions.containsAll(permission.toList())) {
        block()
    } else {
        throw IllegalStateException("User ${session.userId} does not have permissions ${permission.toList()}")
    }
}