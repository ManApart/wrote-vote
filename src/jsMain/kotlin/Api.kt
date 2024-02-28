import dto.Ballet
import dto.Category
import dto.Vote
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

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

suspend fun getVotes(balletId: Int, userId: Int): List<Vote> {
    return getList("ballet/$balletId/$userId/votes", listOf(Vote(0, "Soup"), Vote(1, "Salad")))
}

suspend fun saveVote(votes: List<Vote>) {
    //TODO - post votes to back end to update points
}

suspend inline fun <reified T> getSingle(path: String, default: T): T {
    val result = client.get(path)
    return if (result.status == HttpStatusCode.OK) {
        result.body()
    } else default
}

suspend fun <T> getList(path: String, default: List<T> = emptyList()): List<T> {
    val result = client.get(path)
    return if (result.status == HttpStatusCode.OK) {
        result.body<List<T>>()
    } else default
}