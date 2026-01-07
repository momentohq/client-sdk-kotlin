package software.momento.kotlin.sdk

import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

open class BaseJvmTestClass {
    companion object {
        val cacheName: String = System.getenv("TEST_CACHE_NAME") ?: "kotlin-jvm-integration-${UUID.randomUUID()}"

        lateinit var cacheClient: CacheClient

        @JvmStatic
        @BeforeClass
        fun createCacheClient() {
            cacheClient = CacheClient(
                credentialProvider = CredentialProvider.fromEnvVar("V1_API_KEY"),
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
}
