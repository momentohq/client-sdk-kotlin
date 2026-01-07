package software.momento.example.doc_examples

import kotlinx.coroutines.runBlocking
import software.momento.kotlin.sdk.CacheClient
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.responses.cache.GetResponse
import kotlin.time.Duration.Companion.seconds

fun main() = runBlocking {
    CacheClient(
        CredentialProvider.fromEnvVarV2(),
        Configurations.Laptop.latest,
        60.seconds
    ).use { client ->
        val cacheName = "cache"

        client.createCache(cacheName)

        client.set(cacheName, "key", "value")

        when (val response = client.get(cacheName, "key")) {
            is GetResponse.Hit -> println("Hit: ${response.value}")
            is GetResponse.Miss -> println("Miss")
            is GetResponse.Error -> throw response
        }
    }
}
