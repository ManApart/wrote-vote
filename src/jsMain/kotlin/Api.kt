import dto.Ballet
import dto.Category
import dto.Vote
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString

private val sampleBallet = Ballet(0, "Today's Lunch", 0, 1, 1)

suspend fun getCategories(): List<Category> {
    return getList("categories", listOf(Category(0, "food")))
}

suspend fun getActiveBallets(): List<Ballet> {
    return getList("ballets", listOf(sampleBallet))
}

suspend fun getBallet(id: Int): Ballet {
    return getSingle("ballet/$id", sampleBallet)
}

suspend fun getVotes(balletId: Int): List<Vote> {
    return getList("ballet/$balletId/votes", listOf(Vote(0, "Soup"), Vote(1, "Salad")))
}

suspend fun saveVotes(balletId: Int, votes: List<Vote>): HttpStatusCode {
    return client.put("ballet/$balletId/votes"){
        contentType(ContentType.Application.Json)
        setBody(votes)
    }.status
}

suspend inline fun <reified T> getSingle(path: String, default: T): T {
    val result = client.get(path)
    return if (result.status == HttpStatusCode.OK) {
        result.body()
    } else default
}

suspend inline fun <reified T> getList(path: String, default: List<T> = emptyList()): List<T> {
    val result = client.get(path){
        contentType(ContentType.Application.Json)
    }

    return if (result.status == HttpStatusCode.OK) {
        result.body<List<T>>()
    } else default
}