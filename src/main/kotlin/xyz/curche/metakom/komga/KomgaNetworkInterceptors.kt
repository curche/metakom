package xyz.curche.metakom.komga

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

class BasicAuthInterceptor(id: String, pass: String): Interceptor {
    var credentials: String

    init {
        credentials = Credentials.basic(id, pass)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val newRequestBuilder = oldRequest.newBuilder()
            .addHeader("Authorization", credentials)
        return chain.proceed(newRequestBuilder.build())
    }
}

class UserAgentInterceptor(customUserAgentString: String = "customUserAgent") : Interceptor {

    private val customUserAgent = customUserAgentString

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val newRequestBuilder = oldRequest.newBuilder()
            .addHeader("User-Agent", customUserAgent)
        return chain.proceed(newRequestBuilder.build())
    }
}