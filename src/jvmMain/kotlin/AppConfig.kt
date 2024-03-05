import java.io.File

data class AppConfig(
    val postgresUser: String,
    val postgresPassword: String,
    val authClientId: String,
    val authClientSecret: String,
    val keycloakAuthClientSecret: String,
)

fun readConfig(): AppConfig {
    val raw = File("./.env").readLines().associate {
        val (key, value) = it.split("=")
        key to value
    }
    val rawAuth = File("./auth-secrets.txt").readLines().associate {
        val (key, value) = it.split("=")
        key to value
    }
    return AppConfig(
        raw["POSTGRES_USER"]!!,
        raw["POSTGRES_PASSWORD"]!!,
        rawAuth["auth_client_id"]!!,
        rawAuth["auth_client_secret"]!!,
        rawAuth["keycloak_auth_client_secret"]!!
    )
}