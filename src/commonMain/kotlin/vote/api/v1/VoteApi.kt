package vote.api.v1

import vote.api.*

interface VoteApi {
    suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll
    suspend fun getPoll(id: UUID): Poll?
    suspend fun getResponse(pollId: UUID): PollResponse?
    suspend fun submitResponse(pollId: UUID, response: PollResponse)
    suspend fun getResults(pollId: UUID): PollResults?
}
