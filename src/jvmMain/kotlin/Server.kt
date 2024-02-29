import auth.authRoutes
import auth.configureAuth
import database.initializeDB
import database.seedSampleData
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

val config = readConfig()

val httpClient = HttpClient(CIO) {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }
}

val jsonMapper = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

fun main() {
    val usedPort = System.getenv("PORT")?.toInt() ?: 8080
    println("Wrote Vote started on port $usedPort")

    initializeDB()
    seedSampleData()

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
                get("/") {
                    call.respondText(
                        this::class.java.classLoader.getResource("index.html")!!.readText(),
                        ContentType.Text.Html
                    )
                }
                static("/") {
                    resources("")
                }
                authRoutes()
                authenticate("auth-session") {
                    voteApiRoutes()
                }
            }
        }
    }

    embeddedServer(Netty, environment).start(wait = true)
}
