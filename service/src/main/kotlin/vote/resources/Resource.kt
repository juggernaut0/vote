package vote.resources

import io.ktor.routing.Route

interface Resource {
    fun register(rt: Route)
}
