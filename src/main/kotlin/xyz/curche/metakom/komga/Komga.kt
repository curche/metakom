package xyz.curche.metakom.komga

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.Response
import okhttp3.OkHttpClient
import xyz.curche.metakom.config.Config
import xyz.curche.metakom.komga.dto.UserDto
import xyz.curche.metakom.komga.dto.BookDto
import xyz.curche.metakom.komga.dto.PageWrapperDto
import xyz.curche.metakom.komga.dto.SeriesDto
import xyz.curche.metakom.network.GET
import xyz.curche.metakom.network.interceptor.BasicAuthInterceptor
import xyz.curche.metakom.network.interceptor.UserAgentInterceptor
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

open class Komga(config: Config) {

    private val baseUrl: String = config.baseUrl
    private val username: String = config.username
    private val password: String = config.password
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(UserAgentInterceptor(DEFAULT_USER_AGENT))
        .addInterceptor(BasicAuthInterceptor(username, password))
        .build()

    fun checkUsers(): String {
        val userRequest = GET("$baseUrl/api/v1/users")
        val response = client.newCall(userRequest).execute()

        if (response.code == 200) {
            val userList = json.decodeFromString<List<UserDto>>(response.body!!.string())
            val user = userList[0]
            return "User ${user.email} with ${user.id} has roles ${user.roles}"
        }

        return "ERROR"
    }

    fun verifyUser(): Boolean {
        val request = GET("$baseUrl/api/v1/users/me")
        val response = client.newCall(request).execute()

        if (response.code == 200) {
            val user = json.decodeFromString<UserDto>(response.body!!.string())
            return user.roles.any { it == "ADMIN" }
        }
        return false
    }

    // pages indexed 0...N
    private fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/api/v1/series?page=${page}&deleted=false")

    fun fetchPopularManga(page: Int): List<SeriesDto> {
        val response = client.newCall(popularMangaRequest(page)).execute()
        if (response.code != 200) {
            throw IllegalStateException(" ERROR! Response code ${response.code}(?)")
        }

        return processSeriesPage(response)
    }

    fun fetchBooksForSeries(seriesId: String): List<String> {
        val bookResponse = client.newCall(bookListRequest(seriesId)).execute()
        if (bookResponse.code != 200) {
            throw IllegalStateException(" ERROR! Response code ${bookResponse.code}(?)")
        }

        return bookListParse(bookResponse).map { book -> book.id }
    }

    private fun bookListRequest(seriesId: String): Request =
        GET("$baseUrl/api/v1/series/$seriesId/books?unpaged=true&media_status=READY&deleted=false")

    private fun bookListParse(response: Response): List<BookDto> {
        val bookResponseBody = response.body ?: throw IllegalStateException("Response code ${response.code}")

        return json.decodeFromString<PageWrapperDto<BookDto>>(bookResponseBody.string()).content
    }

    private fun processSeriesPage(response: Response): List<SeriesDto> {
        val responseBody = response.body ?: throw IllegalStateException("Response code ${response.code}")

        return json.decodeFromString<PageWrapperDto<SeriesDto>>(responseBody.string()).content
    }

    companion object {
        const val DEFAULT_USER_AGENT = "curche's metakom"
    }
}