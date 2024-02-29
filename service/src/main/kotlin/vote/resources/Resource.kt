package vote.resources

import io.javalin.Javalin

interface Resource {
    fun register(app: Javalin)
}

fun Javalin.registerResource(resource: Resource): Javalin = also { resource.register(it) }
