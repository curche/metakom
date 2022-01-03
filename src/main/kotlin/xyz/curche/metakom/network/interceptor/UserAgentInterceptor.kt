package xyz.curche.metakom.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

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