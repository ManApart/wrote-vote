import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val redirects = mutableMapOf<String, String>()
val userSessions = mutableMapOf<String, UserSession>()

data class UserSession(val state: String, val token: String) : Principal

fun Application.configureAuth(){
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
//        basic("auth-basic") {
//            realm = "Access to the '/' path"
//            validate { credentials ->
//                if (credentials.name == "bob" && credentials.password == "bob") {
//                    UserIdPrincipal(credentials.name)
//                } else {
//                    null
//                }
//            }
//        }

        session<UserSession>("auth-session") {
            validate { session ->
                if(userSessions[session.state]?.token == session.token ){
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
                    accessTokenUrl = "http://127.0.0.1:4444/oauth2/token",
                    accessTokenInterceptor = {
                                             println("token")
                    },
                    requestMethod = HttpMethod.Post,
                    clientId = "0358d9a1-7f9b-4843-a227-4f5f116b492b",
                    clientSecret = "3vWUF-wtquk5tbphLMK49Tbw7r",
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


suspend fun getSession(
    call: ApplicationCall
): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    //if there is no session, redirect to login
    if (userSession == null) {
        val redirectUrl = URLBuilder("http://localhost:8080/login").run {
            parameters.append("redirectUrl", call.request.uri)
            build()
        }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}

suspend fun getPersonalGreeting(
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

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String,
    val locale: String
)