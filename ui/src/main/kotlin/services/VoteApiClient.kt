package services

import kotlinx.serialization.internal.UnitSerializer
import org.w3c.fetch.Headers
import vote.api.UUID
import vote.api.v1.*
import vote.util.nullable

class VoteApiClient(private val authSupplier: () -> String) : VoteApi {
    private fun headers(): Headers {
        return Headers().apply {
            append("Authorization", "Bearer ${authSupplier()}")
        }
    }

    override suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll {
        return fetch("POST", "/api/v1/polls", pollCreateRequest, PollCreateRequest.serializer(), Poll.serializer(), headers = headers())
    }

    override suspend fun getPoll(id: UUID): Poll? {
        return fetch("GET", "/api/v1/polls/$id", Poll.serializer().nullable, headers = headers())
    }

    override suspend fun getResponse(pollId: UUID): PollResponse? {
        return fetch("GET", "/api/v1/polls/$pollId/response", PollResponse.serializer().nullable, headers = headers())
    }

    override suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        return fetch("PUT", "/api/v1/polls/$pollId/response", response, PollResponse.serializer(), UnitSerializer, headers = headers())
    }

    override suspend fun getResults(pollId: UUID): PollResults? {
        return fetch("GET", "/api/v1/polls/$pollId/results", PollResults.serializer().nullable, headers = headers())
    }
}
