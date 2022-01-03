package xyz.curche.metakom.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class RateLimitInterceptor(
    private val permits: Int,
    period: Long = 1,
    unit: TimeUnit = TimeUnit.MINUTES
) : Interceptor {

    private val requestQueue = ArrayList<Long>(permits)
    private val rateLimitMillis = unit.toMillis(period)

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(requestQueue) {
            val now = System.currentTimeMillis()
            val waitTime = if (requestQueue.size < permits) {
                0
            } else {
                val oldestRequest = requestQueue[0]
                val newestReq = requestQueue[permits - 1]

                if (newestReq - oldestRequest > rateLimitMillis) {
                    0
                } else {
                    oldestRequest + rateLimitMillis - now
                }
            }

            if (requestQueue.size == permits) {
                requestQueue.removeAt(0)
            }
            if (waitTime > 0) {
                requestQueue.add(now + waitTime)
                Thread.sleep(waitTime)
            } else {
                requestQueue.add(now)
            }
        }

        return chain.proceed(chain.request())
    }
}