package auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AuthToken(val sub: String, val aud: List<String>)

@Serializable
data class JWTWrapper(
    @SerialName("access_token") val accessToken: String,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    val scope: String
)

@Serializable
data class KeycloakJWT(
    val sub: String,
    val azp: String,
    val name: String,
) {
    fun validate(clientId: String) {
        if (azp != clientId) throw IllegalStateException("Token is for wrong audience!")
    }
}

@Serializable
data class HydraJWT(
    val sub: String,
    val aud: List<String>
){
    fun validate(clientId: String) {
        if (!aud.contains(clientId)) throw IllegalStateException("Token is for wrong audience!")
    }
}
