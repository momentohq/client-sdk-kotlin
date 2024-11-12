package software.momento.kotlin.sdk


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LimitExceededExceptionTest: BaseAndroidTestClass() {

    @Test
    fun shouldFailWithResourceExhaustedMessage() = runTest {
        val key = "cache";
        val value = 'x'.toString().repeat(5_300_000) // 5.3MB

        val setResponse = cacheClient.set(cacheName, key, value)
        val stringifiedResponse = setResponse.toString()
        assert(stringifiedResponse.contains("Request size limit exceeded for this account"))
    }
}
