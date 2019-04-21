package services

import org.w3c.fetch.Headers
import vote.api.UUID
import vote.api.v1.*

class VoteApiClient(private val authSupplier: () -> String) {
    private fun headers(): Headers {
        return Headers().apply {
            append("Authorization", "Bearer ${authSupplier()}")
        }
    }

    suspend fun getPollHistory(): PollHistory {
        return getPollHistory.call(headers = headers())
    }

    suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll {
        return createPoll.call(pollCreateRequest, headers = headers())
    }

    suspend fun getPoll(id: UUID): Poll? {
        return getPoll.call(mapOf("id" to id))
    }

    suspend fun isCreator(id: UUID): Boolean {
        return isCreator.call(mapOf("id" to id), headers = headers())
    }

    suspend fun getResponse(pollId: UUID): PollResponse? {
        return getResponse.call(mapOf("id" to pollId), headers = headers())
    }

    suspend fun getResponses(pollId: UUID): List<PollResponseDetails> {
        return getResponses.call(mapOf("id" to pollId), headers = headers())
    }

    suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        return submitResponse.call(response, mapOf("id" to pollId), headers = headers())
    }

    suspend fun deactivateResponse(pollId: UUID, responseId: UUID) {
        return deactivateResponse.call(mapOf("id" to pollId, "respId" to responseId), headers = headers())
    }

    suspend fun getResults(pollId: UUID): PollResults? {
        return getResults.call(mapOf("id" to pollId))
    }
}
