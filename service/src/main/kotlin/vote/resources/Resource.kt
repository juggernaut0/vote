package vote.resources

import io.javalin.Javalin

interface Resource {
    fun register(app: Javalin)
}
