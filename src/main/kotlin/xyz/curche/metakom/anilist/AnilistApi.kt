package xyz.curche.metakom.anilist

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.curche.metakom.network.POST
import xyz.curche.metakom.network.interceptor.RateLimitInterceptor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit.MINUTES

class AnilistApi {

    private val client = OkHttpClient.Builder()
        .addInterceptor(RateLimitInterceptor(permits = 85, period = 1, unit = MINUTES))
        .build()
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    fun searchById(searchId: Int): ALMetadata {
        val query = """
        |query Search(${'$'}searchId: Int) {
            |Media(id: ${'$'}searchId, type: MANGA, format_not_in: [NOVEL], sort: SEARCH_MATCH) {
                |id
                |idMal
                |title {
                    |english
                    |romaji
                |}
                |format
                |status(version: 2)
                |description
                |countryOfOrigin
                |source(version: 2)
                |genres
                |staff(sort: RELEVANCE, page: 1){
                    |edges {
                        |node {
                            |name {
                                |full
                            |}
                            |languageV2
                        |}
                        |role
                    |}
                |}
                |isAdult
                |siteUrl
                |chapters
                |volumes
                |tags {
                    |name
                    |category
                    |isGeneralSpoiler
                    |rank
                |}
            |}
        |}
        |""".trimMargin()
        val payload = buildJsonObject {
            put("query", query)
            putJsonObject("variables") {
                put("searchId", searchId)
            }
        }

        return client
            .newCall(POST(apiUrl, body = payload.toString().toRequestBody(jsonMime)))
            .execute()
            .let { response ->
                val responseBody = response.body?.string().orEmpty()
                val data = json.decodeFromString<ALResponse<ALMedia>>(responseBody).data
                val metadata = data.Media
                metadata
            }
    }

    fun searchAndSelectId(searchString: String): Int {
        val searchResults = search(searchString)

        searchResults.mapIndexed { index: Int, it: ALSearchResult ->
            println("$index - ${it.anilist_id} - ${it.title_english} | ${it.title_romaji}: ${it.start_date_fuzzy}, ${it.publication_status.lowercase()}, (${it.country_of_origin})")
        }
        println("Select the corresponding ID: ")
        val choice = readLine()!!.toIntOrNull() ?: 0
        return searchResults[choice].anilist_id
    }

    private fun search(searchString: String): List<ALSearchResult> {
        val query = """
        |query Search(${'$'}query: String) {
            |Page (perPage: 3) {
                |media(search: ${'$'}query, type: MANGA, format_not_in: [NOVEL], sort: SEARCH_MATCH) {
                    |id
                    |idMal
                    |title {
                        |english
                        |romaji
                    |}
                    |format
                    |status(version: 2)
                    |countryOfOrigin
                    |startDate {
                        |year
                        |month
                        |day
                    |}
                    |isAdult
                |}
            |}
        |}
        |""".trimMargin()
        val payload = buildJsonObject {
            put("query", query)
            putJsonObject("variables") {
                put("query", searchString)
            }
        }

        return client
            .newCall(POST(apiUrl, body = payload.toString().toRequestBody(jsonMime)))
            .execute()
            .let { response ->
                val responseBody = response.body?.string().orEmpty()
                val responseBodyAsJson = json.decodeFromString<JsonObject>(responseBody)
                val data = responseBodyAsJson["data"]!!.jsonObject
                val page = data["Page"]!!.jsonObject
                val media = page["media"]!!.jsonArray
                val entries = media.map { jsonToALSearchResult(it.jsonObject) }
                entries
            }
    }

    private fun jsonToALSearchResult(struct: JsonObject): ALSearchResult {
        return ALSearchResult(
            struct["id"]!!.jsonPrimitive.int,
            struct["idMal"]?.jsonPrimitive?.intOrNull ?: 0,
            struct["title"]!!.jsonObject["romaji"]!!.jsonPrimitive.content,
            struct["title"]!!.jsonObject["english"]?.jsonPrimitive?.contentOrNull ?: "na",
            struct["format"]!!.jsonPrimitive.content.replace("_", "-"),
            struct["status"]!!.jsonPrimitive.content,
            struct["countryOfOrigin"]!!.jsonPrimitive.content,
            parseDate(struct, "startDate"),
            struct["isAdult"]!!.jsonPrimitive.boolean
        )
    }

    private fun parseDate(struct: JsonObject, dateKey: String): String {
        return try {
            val date = Calendar.getInstance()
            date.set(
                struct[dateKey]!!.jsonObject["year"]!!.jsonPrimitive.int,
                struct[dateKey]!!.jsonObject["month"]!!.jsonPrimitive.int - 1,
                struct[dateKey]!!.jsonObject["day"]!!.jsonPrimitive.int
            )
            dateFormat.format(date.time)
        } catch (_: Exception) {
            "NA"
        }
    }

    companion object {
        private const val apiUrl = "https://graphql.anilist.co/"
        private const val baseMangaUrl = "https://anilist.co/manga"
        private val jsonMime = "application/json; charset=utf-8".toMediaType()
        private val dateFormat = SimpleDateFormat("YYYY/MM/DD")

        fun mangaUrl(mangaId: Int): String {
            return baseMangaUrl + mangaId
        }
    }
}