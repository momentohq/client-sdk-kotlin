package software.momento.kotlin.sdk

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LimitExceededExceptionTest: BaseJvmTestClass() {

    @Test
    fun shouldFailWithResourceExhaustedMessage() = runTest {
        val key = "cache";
        val value = 'x'.toString().repeat(5_300_000) // 5.3MB

        val setResponse = cacheClient.set(cacheName, key, value)
        val stringifiedResponse = setResponse.toString()
        assert(stringifiedResponse.contains("Request size limit exceeded for this account"))
    }
}
