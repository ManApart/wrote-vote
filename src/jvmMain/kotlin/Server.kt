import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

val httpClient = HttpClient(CIO) {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }
}

fun main() {
    val usedPort = System.getenv("PORT")?.toInt() ?: 8080
    println("Wrote Vote started on port $usedPort")

//    initializeDB()
//    writeTest()
//    readTest()
//    println("DB Tests done")

    val environment = applicationEngineEnvironment {
        connector {
            port = usedPort
        }

        module {
            install(Sessions) {
                cookie<UserSession>("user_session")
            }
            val redirects = mutableMapOf<String, String>()
            install(Authentication) {
                basic("auth-basic") {
                    realm = "Access to the '/' path"
                    validate { credentials ->
                        if (credentials.name == "bob" && credentials.password == "bob") {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }
                    }
                }

                session<UserSession>("auth-session") {
                    validate { session ->
                        session
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
                            requestMethod = HttpMethod.Post,
                            clientId = "715f5a53-7aa7-44c3-b827-2dfa4c01e776",
                            clientSecret = "6umRg4u_2JB80D.y.7qJ_pQLTW",
                            defaultScopes = listOf("offline"),
                            extraAuthParameters = listOf("access_type" to "offline"),
                            onStateCreated = { call, state ->
                                call.request.queryParameters["redirectUrl"]?.let {
                                    redirects[state] = it
                                }
                            }
                        )
                    }
                    client = httpClient
                }
            }
            install(ContentNegotiation) {
                json()
            }
            install(CORS) {
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Delete)
                anyHost()
            }
            install(Compression) {
                gzip()
            }
            install(io.ktor.server.plugins.partialcontent.PartialContent)
            install(AutoHeadResponse)
            routing {
                authenticate("auth-oauth-hydra") {
                    get("/login") { }
                }
                get("/callback") {
                    val code = call.request.queryParameters["code"]
                    val state = call.request.queryParameters["state"]
                    val scopes = call.request.queryParameters["scope"]
                    println("$code $state $scopes")
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    println("Prince: ${principal?.accessToken}")

                    if (code != null && state != null) {
                        call.sessions.set(UserSession(state, code))
                        redirects[state]?.let { redirect ->
                            call.respondRedirect(redirect)
                            return@get
                        }
                    }
                    call.respondRedirect("/home")
                }

                //This properly gets principal
                authenticate("auth-session") {
                    get("/home") {
                        val principal = call.principal<UserSession>()!!
                        val userInfo = getPersonalGreeting(httpClient, principal)
                        println("info: $userInfo")
                        call.respondText("Hello, ${userInfo}! Welcome home!")
                    }
                }

                authenticate("auth-basic") {
                    get("/home2") {
                        call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")

                    }
                }

                get("/") {
                    call.respondText(
                        this::class.java.classLoader.getResource("index.html")!!.readText(),
                        ContentType.Text.Html
                    )
                }
                static("/") {
                    resources("")
                }
            }
        }
    }

    embeddedServer(Netty, environment).start(wait = true)
}
