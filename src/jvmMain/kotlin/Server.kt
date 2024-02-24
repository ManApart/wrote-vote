import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
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
            configureAuth()
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
                    val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                    println("$code $state $scopes $currentPrincipal")

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
//                        val principal: OAuthAccessTokenResponse.OAuth2? = tokenRequest.body()
                    val principal: Map<String, String>? = tokenRequest.body()
//                        val principal = tokenRequest.bodyAsText()

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
                        println("info: $userInfo")
                        call.respondText("Hello, ${userInfo}! Welcome home!")
                    }
                }
//
//                authenticate("auth-basic") {
//                    get("/home2") {
//                        call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
//
//                    }
//                }

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
