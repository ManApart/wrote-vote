package dto

import kotlinx.serialization.Serializable

@Serializable
data class Category(val id: Int, val name: String)

@Serializable
data class Ballet(
    val id: Int,
    val name: String,
    val category: Int,
    val points: Int,
    val pointsPerChoice: Int,
    val opened: String? = "02/25/2024",
    val closed: String? = null,
)

@Serializable
data class BalletCandidate(
    val id: Int,
    val name: String,
)

@Serializable
data class Vote(
    val id: Int,
    val selectionName: String,
    var points: Int = 0,
//    var submitted: String,
//    var revoked: String
)

//@Serializable
//data class UserBallet(
//    val balletId: Int,
//    val name: String,
//)