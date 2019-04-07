package vote.api.v1

import vote.api.*

interface VoteApi {
    suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll
    suspend fun getPoll(id: UUID): Poll?
    suspend fun isCreator(id: UUID): Boolean
    suspend fun getResponse(pollId: UUID): PollResponse?
    suspend fun getResponses(pollId: UUID): List<PollResponseDetails>
    suspend fun submitResponse(pollId: UUID, response: PollResponse)
    suspend fun deactivateResponse(pollId: UUID, responseId: UUID)
    suspend fun getResults(pollId: UUID): PollResults?
    suspend fun getPollHistory(): PollHistory
}
