import dto.Category
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun getCategories(): List<Category> {
    return getList("categories", listOf(Category("food")))
}

suspend fun <T> getList(path: String, default: List<T> = emptyList()): List<T>{
    val result = client.get(path)
    return if (result.status == HttpStatusCode.OK) {
        result.body<List<T>>()
    } else default
}