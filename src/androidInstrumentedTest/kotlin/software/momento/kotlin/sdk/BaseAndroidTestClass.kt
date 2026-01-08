package software.momento.kotlin.sdk

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

open class BaseAndroidTestClass {
    companion object {
        @JvmStatic
        protected lateinit var credentialProvider: CredentialProvider
        lateinit var cacheClient: CacheClient
        lateinit var cacheName: String

        @JvmStatic
        @BeforeClass
        fun createCacheClient() {
            // Instrumented Android tests cannot access environment variables directly, so we
            // pass them in as arguments to the test runner. They are defined in build.gradle.kts.
            val arguments = InstrumentationRegistry.getArguments()
            val apiKey = arguments.getString("V1ApiKey")!!
            cacheName = arguments.getString("TestCacheName")!!
            credentialProvider = CredentialProvider.fromString(apiKey)
            cacheClient = CacheClient(
                credentialProvider = credentialProvider,
                configuration = Configurations.Mobile.latest,
                itemDefaultTtl = 60.seconds
            )

            runBlocking { cacheClient.createCache(cacheName) }
        }

        @JvmStatic
        @AfterClass
        fun destroyCacheClient() {
            runBlocking { cacheClient.deleteCache(cacheName) }
            cacheClient.close()
        }
    }
}
