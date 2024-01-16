package software.momento.example.doc_examples

import kotlinx.coroutines.runBlocking
import software.momento.kotlin.sdk.CacheClient
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.responses.cache.GetResponse
import kotlin.time.Duration.Companion.seconds

fun main() = runBlocking {
    CacheClient(
        CredentialProvider.fromEnvVar("MOMENTO_API_KEY"),
        Configurations.Laptop.latest,
        60.seconds
    ).use { client ->
        //...
    }
}
