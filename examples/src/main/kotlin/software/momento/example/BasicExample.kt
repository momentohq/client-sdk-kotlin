package software.momento.example

import kotlinx.coroutines.runBlocking
import software.momento.kotlin.sdk.CacheClient
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.responses.cache.GetResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheListResponse
import kotlin.time.Duration.Companion.seconds


private val cacheName = "cache"
private val key = "key"
private val value = "value"

fun main() = runBlocking {
    printStartBanner()

    CacheClient(
        CredentialProvider.fromEnvVarV2(), Configurations.Laptop.latest, 60.seconds
    ).use { client ->
        println("Creating cache '$cacheName'")
        when (val response = client.createCache(cacheName)) {
            is CacheCreateResponse.Success -> println("Cache '$cacheName' created")
            is CacheCreateResponse.AlreadyExists -> println("Cache '$cacheName' already exists")
            is CacheCreateResponse.Error -> {
                println("An error occurred while attempting to create cache 'test-cache': ${response.errorCode}")
                println(response.message)
            }
        }

        println("Listing caches:")
        when (val response: CacheListResponse = client.listCaches()) {
            is CacheListResponse.Success -> {
                val caches: String = response.caches.joinToString("\n") { cacheInfo -> cacheInfo.name }
                println(caches)
            }

            is CacheListResponse.Error -> {
                println("An error occurred while attempting to list caches: ${response.errorCode}")
                println(response.message)
            }
        }

        println("setting key '$key', value '$value'")
        client.set(cacheName, key, value)

        println("getting value for key '$key'")
        when (val response = client.get(cacheName, key)) {
            is GetResponse.Hit -> println("Hit: ${response.value}")
            is GetResponse.Miss -> println("Miss")
            is GetResponse.Error -> {
                println("An error occurred while attempting to get '$key': ${response.errorCode}")
                println(response.message)
            }
        }
    }
    printEndBanner()
}

private fun printStartBanner() {
    println("******************************************************************")
    println("Basic Example Start")
    println("******************************************************************")
}

private fun printEndBanner() {
    println("******************************************************************")
    println("Basic Example End")
    println("******************************************************************")
}
