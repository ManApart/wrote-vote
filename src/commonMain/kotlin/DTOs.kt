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
    val opened: String? = "02/25/2024",
    val closed: String? = null,
)