package services

import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.browser.window

suspend fun fetch(method: String, path: String, body: String? = undefined, headers: Headers? = undefined): String {
    val resp = window.fetch(path, RequestInit(method = method, body = body, headers = headers)).await()
    if (!resp.ok) throw FetchException("${resp.status} ${resp.statusText}", resp.status)
    return resp.text().await()
}

suspend fun <R> fetch(method: String, path: String, des: DeserializationStrategy<R>, headers: Headers? = undefined): R {
    return fetch(method, path, headers = headers).let { Json.nonstrict.parse(des, it) }
}

suspend fun <R, T> fetch(method: String,
                         path: String,
                         body: T, ser: SerializationStrategy<T>,
                         des: DeserializationStrategy<R>,
                         headers: Headers? = undefined): R {
    val json = Json.stringify(ser, body)
    return fetch(method, path, json, headers = headers).let { Json.nonstrict.parse(des, it) }
}
