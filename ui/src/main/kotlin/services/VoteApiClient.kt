package services

import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import vote.api.*
import vote.api.v1.*
import vote.util.nullable
import kotlin.browser.window

class VoteApiClient : VoteApi {
    private suspend fun <R> fetch(method: String, path: String, des: DeserializationStrategy<R>): R {
        return window.fetch("/api/v1$path", RequestInit(method = method))
                .then {
                    if (!it.ok) throw RuntimeException("${it.status} ${it.statusText}")
                    it.text()
                }
                .then { Json.parse(des, it) }
                .await()
    }

    private suspend fun <R, T> fetch(method: String, path: String, body: T, ser: SerializationStrategy<T>, des: DeserializationStrategy<R>): R {
        val json = Json.stringify(ser, body)
        return window.fetch("/api/v1$path", RequestInit(method = method, body = json))
                .then {
                    if (!it.ok) throw RuntimeException("${it.status} ${it.statusText}")
                    it.text()
                }
                .then { Json.parse(des, it) }
                .await()
    }

    override suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll {
        return fetch("POST", "/polls", pollCreateRequest, PollCreateRequest.serializer(), Poll.serializer())
    }

    override suspend fun getPoll(id: UUID): Poll? {
        return fetch("GET", "/polls/$id", Poll.serializer().nullable)
    }

    // TODO add userId
    override suspend fun getResponse(pollId: UUID): PollResponse? {
        return fetch("GET", "/polls/$pollId/response", PollResponse.serializer().nullable)
    }

    override suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        return fetch("PUT", "/polls/$pollId/response", response, PollResponse.serializer(), UnitSerializer)
    }

    override suspend fun getResults(pollId: UUID): PollResults? {
        return fetch("GET", "/polls/$pollId/results", PollResults.serializer().nullable)
    }
}
