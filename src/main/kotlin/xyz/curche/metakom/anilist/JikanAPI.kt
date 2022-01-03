package xyz.curche.metakom.anilist

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import xyz.curche.metakom.network.GET
import xyz.curche.metakom.network.interceptor.RateLimitInterceptor
import java.util.concurrent.TimeUnit

class JikanAPI {

    private val client = OkHttpClient.Builder()
        .addInterceptor(RateLimitInterceptor(permits = 3, period = 1, unit = TimeUnit.SECONDS))
        .build()

    private val json by lazy { Json { ignoreUnknownKeys = true } }

    fun getSerializationFromID(malId: Int): String {
        val response = client.newCall(GET("$apiUrl/manga/$malId")).execute()
        val responseBody = response.body?.string().orEmpty()
        val responseBodyAsJson = json.decodeFromString<JsonObject>(responseBody)
        val data = responseBodyAsJson["data"]!!.jsonObject
        val media = data["serializations"]!!.jsonArray
        return media
            .map { it.jsonObject["name"] }
            .joinToString(separator = ", ")
    }

    companion object {
        private const val apiUrl = "https://api.jikan.moe/v4"
        private val jsonMime = "application/json; charset=utf-8".toMediaType()
    }
}