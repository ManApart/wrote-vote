package dto

import kotlinx.serialization.Serializable

@Serializable
data class Category(val id: Int, val name: String)

@Serializable
data class Candidate(val name: String, val categoryId: Int, val id: Int? = null)

@Serializable
data class Ballot(
    val name: String,
    val category: Int,
    val points: Int,
    val pointsPerChoice: Int,
    val opened: String? = "02/25/2024",
    val closed: String? = null,
    val id: Int? = null,
)

@Serializable
data class BallotCandidate(
    val candidate: Int,
)

@Serializable
data class Vote(
    val id: Int,
    val selectionName: String,
    var points: Int = 0,
)