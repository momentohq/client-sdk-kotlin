package software.momento.kotlin.sdk.exceptions

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.BeforeClass
import software.momento.kotlin.sdk.CacheClient
import software.momento.kotlin.sdk.UsingTestRunner
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.time.Duration.Companion.seconds

class LimitExceededExceptionTest : UsingTestRunner() {

    companion object {
        val cacheName: String = System.getenv("TEST_CACHE_NAME") ?: "kotlin-jvm-integration-${UUID.randomUUID()}"

        lateinit var cacheClient: CacheClient

        @JvmStatic
        @BeforeClass
        fun createCacheClient() {
            cacheClient = CacheClient(
                credentialProvider = CredentialProvider.fromEnvVar("TEST_API_KEY"),
                configuration = Configurations.Laptop.latest,
                itemDefaultTtl = 60.seconds
            )

            runBlocking {
                val cacheClientResponse = cacheClient.createCache(cacheName)
                if (cacheClientResponse is CacheCreateResponse.Error) {
                    throw RuntimeException("Could not create cache for tests: " + cacheClientResponse.message)
                }
            }
        }

        @JvmStatic
        @AfterClass
        fun destroyCacheClient() {
            runBlocking { cacheClient.deleteCache(cacheName) }
            cacheClient.close()
        }
    }

    @Test
    fun shouldFailWithResourceExhaustedMessage() = runTest {
        val key = "cache";
        val value = 'x'.toString().repeat(5_300_000) // 5.3MB

        val setResponse = cacheClient.set(cacheName, key, value)
        val stringifiedResponse = setResponse.toString()
        assert(stringifiedResponse.contains("Request size limit exceeded for this account"))
    }
}

