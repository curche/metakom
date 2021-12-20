package xyz.curche.metakom.komga

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import xyz.curche.metakom.config.Config
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

private val DEFAULT_CACHE_CONTROL = CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build()
private val DEFAULT_HEADERS = Headers.Builder().build()
private val DEFAULT_BODY: RequestBody = FormBody.Builder().build()
const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"

fun GET(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .cacheControl(cache)
        .build()
}

fun POST(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    body: RequestBody = DEFAULT_BODY,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
) : Request {
    return Request.Builder()
        .url(url)
        .post(body)
        .headers(headers)
        .cacheControl(cache)
        .build()
}

fun PATCH(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    body: RequestBody = DEFAULT_BODY,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request {
    return Request.Builder()
        .url(url)
        .patch(body)
        .headers(headers)
        .cacheControl(cache)
        .build()
}

class BasicAuthInterceptor(id: String, pass: String): Interceptor {
    lateinit var credentials: String

    init {
        credentials = Credentials.basic(id, pass)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val newRequestBuilder = oldRequest.newBuilder()
            .header("Authorization", credentials)
        return chain.proceed(newRequestBuilder.build())
    }
}

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val roles: List<String>,
)

open class Komga(config: Config) {

    private val baseUrl: String = config.baseUrl
    private val username: String = config.username
    private val password: String = config.password
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    private fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(BasicAuthInterceptor(username, password))
        .build()

    private val headers: Headers by lazy { headersBuilder().build() }

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
    // fun popularMangaRequest(page: Int): Request = GET("")

    companion object {
        const val DEFAULT_USER_AGENT = "curcheMetakom"
    }
}