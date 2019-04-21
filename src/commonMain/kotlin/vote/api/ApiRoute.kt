package vote.api

import kotlinx.serialization.KSerializer

class ApiRoute<T, R> private constructor(
        val method: Method,
        val path: PathTemplate,
        val responseSer: KSerializer<R>,
        val requestSer: KSerializer<T>? = null
) {
    companion object {
        operator fun <R> invoke(method: Method, path: PathTemplate, responseSer: KSerializer<R>) : ApiRoute<Nothing, R> {
            return ApiRoute(method, path, responseSer)
        }

        operator fun <T, R> invoke(method: Method, path: PathTemplate, responseSer: KSerializer<R>, requestSer: KSerializer<T>) : ApiRoute<T, R> {
            return ApiRoute(method, path, responseSer, requestSer)
        }
    }
}

enum class Method { GET, POST, PUT, DELETE }

class PathTemplate internal constructor(private val segments: List<Segment>) {
    fun applyParams(params: Map<String, Any?>): String {
        return "/" + segments.joinToString(separator = "/") {
            when (it) {
                is ConstantSegment -> it.seg
                is ParamSegment -> params[it.name].toString()
            }
        }
    }

    override fun toString(): String {
        return "/" + segments.joinToString(separator = "/")
    }
}
private val paramMatcher = Regex("\\{(\\w+)}")
fun pathOf(path: String): PathTemplate {
    return path
            .split('/')
            .filter { it.isNotBlank() }
            .map {
                val match = paramMatcher.matchEntire(it)
                if (match != null) {
                    ParamSegment(match.groups[1]!!.value)
                } else {
                    ConstantSegment(it)
                }
            }
            .let { PathTemplate(it) }
}

internal sealed class Segment
private data class ConstantSegment(val seg: String) : Segment() {
    override fun toString(): String {
        return seg
    }
}
private data class ParamSegment(val name: String) : Segment() {
    override fun toString(): String {
        return "{$name}"
    }
}
