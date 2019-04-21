package services

import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import vote.api.ApiRoute
import kotlin.browser.window

suspend fun fetch(method: String, path: String, body: String? = undefined, headers: Headers? = undefined): String {
    val resp = window.fetch(path, RequestInit(method = method, body = body, headers = headers)).await()
    val text = resp.text().await()
    if (!resp.ok) throw FetchException("${resp.status} ${resp.statusText}", resp.status, text)
    return text
}

suspend fun <R> ApiRoute<Nothing, R>.call(params: Map<String, Any?> = emptyMap(), headers: Headers? = undefined): R {
    return fetch(method.toString(), path.applyParams(params), headers = headers).let { Json.nonstrict.parse(responseSer, it) }
}

suspend fun <T, R> ApiRoute<T, R>.call(body: T, params: Map<String, Any?> = emptyMap(), headers: Headers? = undefined): R {
    @Suppress("UNCHECKED_CAST")
    val rs = requestSer ?: return (this as ApiRoute<Nothing, R>).call(params, headers)
    val json = Json.stringify(rs, body)
    return fetch(method.toString(), path.applyParams(params), body = json, headers = headers)
            .let { Json.nonstrict.parse(responseSer, it) }
}
