package software.momento.example.doc_examples

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import software.momento.kotlin.sdk.CacheClient
import software.momento.kotlin.sdk.TopicClient
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.config.TopicConfigurations
import software.momento.kotlin.sdk.responses.cache.DeleteResponse
import software.momento.kotlin.sdk.responses.cache.GetResponse
import software.momento.kotlin.sdk.responses.cache.SetResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheDeleteResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheListResponse
import software.momento.kotlin.sdk.responses.topic.TopicMessage
import software.momento.kotlin.sdk.responses.topic.TopicPublishResponse
import software.momento.kotlin.sdk.responses.topic.TopicSubscribeResponse
import kotlin.time.Duration.Companion.seconds

const val FAKE_V1_API_KEY =
    "eyJhcGlfa2V5IjogImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSklVekkxTmlKOS5leUpwYzNNaU9pSlBibXhwYm1VZ1NsZFVJRUoxYVd4a1pY" +
            "SWlMQ0pwWVhRaU9qRTJOemd6TURVNE1USXNJbVY0Y0NJNk5EZzJOVFV4TlRReE1pd2lZWFZrSWpvaUlpd2ljM1ZpSWpvaWFuSnZZMnRs" +
            "ZEVCbGVHRnRjR3hsTG1OdmJTSjkuOEl5OHE4NExzci1EM1lDb19IUDRkLXhqSGRUOFVDSXV2QVljeGhGTXl6OCIsICJlbmRwb2ludCI6" +
            "ICJ0ZXN0Lm1vbWVudG9ocS5jb20ifQo="

const val FAKE_V2_API_KEY = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0IjoiZyIsImp0aSI6InNvbWUtaWQifQ.GMr9nA6HE0ttB6llXct_2Sg5-fOKGFbJCdACZFgNbN1fhT6OPg_hVc8ThGzBrWC_RlsBpLA1nzqK3SOJDXYxAw"

suspend fun retrieveAuthTokenFromYourSecretsManager(): String {
    return FAKE_V1_API_KEY
}

suspend fun retrieveApiKeyV2FromYourSecretsManager(): String {
    return FAKE_V2_API_KEY
}

suspend fun example_API_CredentialProviderFromEnvVarV2() {
    CredentialProvider.fromEnvVarV2("MOMENTO_API_KEY", "MOMENTO_ENDPOINT")
}

suspend fun example_API_CredentialProviderFromEnvVarV2Default() {
    CredentialProvider.fromEnvVarV2()
}

suspend fun example_API_CredentialProviderFromApiKeyV2() {
    val apiKey = retrieveApiKeyV2FromYourSecretsManager()
    val endpoint = "cell-4-us-west-2-1.prod.a.momentohq.com"
    CredentialProvider.fromApiKeyV2(apiKey, endpoint)
}

suspend fun example_API_CredentialProviderFromDisposableToken() {
    val authToken = retrieveAuthTokenFromYourSecretsManager()
    CredentialProvider.fromDisposableToken(authToken)
}

suspend fun example_API_CredentialProviderFromEnvVar() {
    CredentialProvider.fromEnvVar("V1_API_KEY")
}

suspend fun example_API_CredentialProviderFromString() {
    val authToken = retrieveAuthTokenFromYourSecretsManager()
    CredentialProvider.fromString(authToken)
}

suspend fun example_API_ConfigurationLaptop() {
    Configurations.Laptop.latest
}

suspend fun example_API_ConfigurationInRegionLatest() {
    Configurations.InRegion.latest
}

suspend fun example_API_ConfigurationLowLatency() {
    Configurations.InRegion.LowLatency.latest
}

suspend fun example_API_InstantiateCacheClient() {
    CacheClient(
        CredentialProvider.fromEnvVarV2(), Configurations.Laptop.latest, 60.seconds
    ).use { cacheClient ->
        //...
    }
}

suspend fun example_API_ErrorHandlingHitMiss(cacheClient: CacheClient) {
    when (val response = cacheClient.get("test-cache", "test-key")) {
        is GetResponse.Hit -> println("Retrieved value for key 'test-key': ${response.value}")
        is GetResponse.Miss -> println("Key 'test-key' was not found in cache 'test-cache'")
        is GetResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to get key 'test-key' from cache 'test-cache': ${response.errorCode}",
            response
        )
    }
}

suspend fun example_API_ErrorHandlingSuccess(cacheClient: CacheClient) {
    when (val response = cacheClient.set("test-cache", "test-key", "test-value")) {
        is SetResponse.Success -> println("Key 'test-key' stored successfully")
        is SetResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to store key 'test-key' in cache 'test-cache': ${response.errorCode}",
            response
        )
    }
}

suspend fun example_API_CreateCache(cacheClient: CacheClient) {
    when (val response = cacheClient.createCache("test-cache")) {
        is CacheCreateResponse.Success -> println("Cache 'test-cache' created")
        is CacheCreateResponse.AlreadyExists -> println("Cache 'test-cache' already exists")
        is CacheCreateResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to create cache 'test-cache': ${response.errorCode}", response
        )
    }
}

suspend fun example_API_DeleteCache(cacheClient: CacheClient) {
    when (val response = cacheClient.deleteCache("test-cache")) {
        is CacheDeleteResponse.Success -> println("Cache 'test-cache' deleted")
        is CacheDeleteResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to delete cache 'test-cache': ${response.errorCode}", response
        )
    }
}

suspend fun example_API_ListCaches(cacheClient: CacheClient) {
    when (val response: CacheListResponse = cacheClient.listCaches()) {
        is CacheListResponse.Success -> {
            val caches: String = response.caches.joinToString("\n") { cacheInfo -> cacheInfo.name }
            println("Caches:\n$caches")
        }

        is CacheListResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to list caches: ${response.errorCode}", response
        )
    }
}

suspend fun example_API_Set(cacheClient: CacheClient) {
    when (val response = cacheClient.set("test-cache", "test-key", "test-value")) {
        is SetResponse.Success -> println("Key 'test-key' stored successfully")
        is SetResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to store key 'test-key' in cache 'test-cache': ${response.errorCode}",
            response
        )
    }
}

suspend fun example_API_Get(cacheClient: CacheClient) {
    when (val response = cacheClient.get("test-cache", "test-key")) {
        is GetResponse.Hit -> println("Retrieved value for key 'test-key': ${response.value}")
        is GetResponse.Miss -> println("Key 'test-key' was not found in cache 'test-cache'")
        is GetResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to get key 'test-key' from cache 'test-cache': ${response.errorCode}",
            response
        )
    }
}

suspend fun example_API_Delete(cacheClient: CacheClient) {
    when (val response = cacheClient.delete("test-cache", "test-key")) {
        is DeleteResponse.Success -> println("Key 'test-key' deleted successfully")
        is DeleteResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to delete key 'test-key' from cache 'test-cache': ${response.errorCode}",
            response
        )
    }
}

suspend fun example_API_InstantiateTopicClient() {
    TopicClient(
        CredentialProvider.fromEnvVarV2(), TopicConfigurations.Laptop.latest
    ).use { topicClient ->
        //...
    }
}

suspend fun example_API_TopicSubscribe(topicClient: TopicClient) {
    when (val response = topicClient.subscribe("test-cache", "test-topic")) {
        is TopicSubscribeResponse.Subscription -> coroutineScope {
            launch {
                withTimeoutOrNull(2000) {
                    response.collect { item ->
                        when (item) {
                            is TopicMessage.Text -> println("Received text message: ${item.value}")
                            is TopicMessage.Binary -> println("Received binary message: ${item.value}")
                            is TopicMessage.Error -> throw RuntimeException(
                                "An error occurred reading messages from topic 'test-topic': ${item.errorCode}", item
                            )
                        }
                    }
                }
            }
        }

        is TopicSubscribeResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to subscribe to topic 'test-topic': ${response.errorCode}", response
        )
    }
}

suspend fun example_API_TopicPublish(topicClient: TopicClient) {
    when (val response = topicClient.publish("test-cache", "test-topic", "test-message")) {
        is TopicPublishResponse.Success -> println("Message published successfully")
        is TopicPublishResponse.Error -> throw RuntimeException(
            "An error occurred while attempting to publish message to topic 'test-topic': ${response.errorCode}",
            response
        )
    }
}

fun main() = runBlocking {
    example_API_CredentialProviderFromEnvVar()
    example_API_CredentialProviderFromString()
    example_API_CredentialProviderFromEnvVarV2Default()
    example_API_CredentialProviderFromEnvVarV2()
    example_API_CredentialProviderFromApiKeyV2()
    example_API_CredentialProviderFromDisposableToken()

    example_API_ConfigurationLaptop()
    example_API_ConfigurationInRegionLatest()
    example_API_ConfigurationLowLatency()

    example_API_InstantiateCacheClient()

    CacheClient(
        CredentialProvider.fromEnvVarV2(), Configurations.Laptop.latest, 60.seconds
    ).use { cacheClient ->
        try {
            example_API_ErrorHandlingHitMiss(cacheClient)
        } catch (e: Exception) {
            println("Hit/Miss error handling succeeded")
            println(e.message)
        }
        try {
            example_API_ErrorHandlingSuccess(cacheClient)
        } catch (e: Exception) {
            println("Success error handling succeeded")
            println(e.message)
        }

        example_API_CreateCache(cacheClient)
        example_API_DeleteCache(cacheClient)
        example_API_CreateCache(cacheClient)
        example_API_ListCaches(cacheClient)

        example_API_Set(cacheClient)
        example_API_Get(cacheClient)
        example_API_Delete(cacheClient)
    }

    example_API_InstantiateTopicClient()
    TopicClient(
        CredentialProvider.fromEnvVarV2(), TopicConfigurations.Laptop.latest
    ).use { topicClient ->
        example_API_TopicSubscribe(topicClient)
        example_API_TopicPublish(topicClient)
    }
}
