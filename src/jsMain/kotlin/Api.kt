import dto.Ballet
import dto.Category
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun getCategories(): List<Category> {
    return getList("categories", listOf(Category(0, "food")))
}

suspend fun getActiveBallets(): List<Ballet> {
    return getList("ballets", listOf(Ballet(0, "Today's Lunch", 0,1)))
}

suspend fun <T> getList(path: String, default: List<T> = emptyList()): List<T>{
    val result = client.get(path)
    return if (result.status == HttpStatusCode.OK) {
        result.body<List<T>>()
    } else default
}