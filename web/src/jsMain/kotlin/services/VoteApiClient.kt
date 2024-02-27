package services

import multiplatform.UUID
import multiplatform.api.FetchClient
import multiplatform.api.FetchHeaders
import multiplatform.api.Headers
import vote.api.v1.*

class VoteApiClient(private val authSupplier: () -> String) {
    private val client = FetchClient()

    private fun headers(): Headers {
        return FetchHeaders("Authorization" to "Bearer ${authSupplier()}")
    }

    suspend fun getPollHistory(): PollHistory {
        return client.callApi(getPollHistory, Unit, headers = headers())
    }

    suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll {
        return client.callApi(createPoll, Unit, pollCreateRequest, headers = headers())
    }

    suspend fun getPoll(id: UUID): Poll? {
        return client.callApi(getPoll, IdParam(id))
    }

    suspend fun isCreator(id: UUID): Boolean {
        return client.callApi(isCreator, IdParam(id), headers = headers())
    }

    suspend fun getResponse(pollId: UUID): PollResponse? {
        return client.callApi(getResponse, IdParam(pollId), headers = headers())
    }

    suspend fun getResponses(pollId: UUID): List<PollResponseDetails> {
        return client.callApi(getResponses, IdParam(pollId), headers = headers())
    }

    suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        return client.callApi(submitResponse, IdParam(pollId), response, headers = headers())
    }

    suspend fun deactivateResponse(pollId: UUID, responseId: UUID) {
        return client.callApi(deactivateResponse, RespParam(pollId, responseId), headers = headers())
    }

    suspend fun getResults(pollId: UUID): PollResults? {
        return client.callApi(getResults, IdParam(pollId))
    }
}
