import dto.Ballot
import dto.Category
import dto.Vote
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString

private val sampleBallot = Ballot("Today's Lunch", 0, 1, 1, id = 0)

suspend fun getCategories(): List<Category> {
    return getList("categories", listOf(Category(0, "food")))
}

suspend fun getActiveBallots(): List<Ballot> {
    return getList("ballots", listOf(sampleBallot))
}

suspend fun getBallot(id: Int): Ballot {
    return getSingle("ballot/$id", sampleBallot)
}

suspend fun getVotes(ballotId: Int): List<Vote> {
    return getList("ballot/$ballotId/votes", listOf(Vote(0, "Soup"), Vote(1, "Salad")))
}

suspend fun saveVotes(ballotId: Int, votes: List<Vote>): HttpStatusCode {
    return client.put("ballot/$ballotId/votes"){
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