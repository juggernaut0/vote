package vote.api.v1

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import multiplatform.UUID
import multiplatform.UUIDSerializer
import multiplatform.api.ApiRoute
import multiplatform.api.Method.*
import multiplatform.api.pathOf

@Serializable
class IdParam(@Serializable(with = UUIDSerializer::class) val id: UUID)
@Serializable
class RespParam(@Serializable(with = UUIDSerializer::class) val id: UUID, @Serializable(with = UUIDSerializer::class) val respId: UUID)

val getPollHistory = ApiRoute(GET, pathOf(Unit.serializer(), "/vote/api/v1/polls"), PollHistory.serializer())
val createPoll = ApiRoute(POST, pathOf(Unit.serializer(), "/vote/api/v1/polls"), Poll.serializer(), PollCreateRequest.serializer())
val getPoll = ApiRoute(GET, pathOf(IdParam.serializer(), "/vote/api/v1/polls/{id}"), Poll.serializer().nullable)
val isCreator = ApiRoute(GET, pathOf(IdParam.serializer(), "/vote/api/v1/polls/{id}/creator"), Boolean.serializer())
val getResponse = ApiRoute(GET, pathOf(IdParam.serializer(), "/vote/api/v1/polls/{id}/response"), PollResponse.serializer().nullable)
val submitResponse = ApiRoute(PUT, pathOf(IdParam.serializer(), "/vote/api/v1/polls/{id}/response"), Unit.serializer(), PollResponse.serializer())
val getResponses = ApiRoute(GET, pathOf(IdParam.serializer(), "/vote/api/v1/polls/{id}/responses"), ListSerializer(PollResponseDetails.serializer()))
val deactivateResponse = ApiRoute(DELETE, pathOf(RespParam.serializer(), "/vote/api/v1/polls/{id}/responses/{respId}"), Unit.serializer())
val getResults = ApiRoute(GET, pathOf(IdParam.serializer(), "/vote/api/v1/polls/{id}/results"), PollResults.serializer().nullable)
