package vote.api.v1

import juggernaut0.mutliplatform.api.ApiRoute
import juggernaut0.mutliplatform.api.Method.*
import juggernaut0.mutliplatform.api.pathOf
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import vote.util.nullable

val getPollHistory = ApiRoute(GET, pathOf("/api/v1/polls"), PollHistory.serializer())
val createPoll = ApiRoute(POST, pathOf("/api/v1/polls"), Poll.serializer(), PollCreateRequest.serializer())
val getPoll = ApiRoute(GET, pathOf("/api/v1/polls/{id}"), Poll.serializer().nullable)
val isCreator = ApiRoute(GET, pathOf("/api/v1/polls/{id}/creator"), Boolean.serializer())
val getResponse = ApiRoute(GET, pathOf("/api/v1/polls/{id}/response"), PollResponse.serializer().nullable)
val submitResponse = ApiRoute(PUT, pathOf("/api/v1/polls/{id}/response"), UnitSerializer, PollResponse.serializer())
val getResponses = ApiRoute(GET, pathOf("/api/v1/polls/{id}/responses"), PollResponseDetails.serializer().list)
val deactivateResponse = ApiRoute(DELETE, pathOf("/api/v1/polls/{id}/responses/{respId}"), UnitSerializer)
val getResults = ApiRoute(GET, pathOf("/api/v1/polls/{id}/results"), PollResults.serializer().nullable)

val signIn = ApiRoute(POST, pathOf("/api/v1/users"), UnitSerializer, String.serializer())
