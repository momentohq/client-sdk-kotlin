package software.momento.kotlin.sdk.internal

import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configuration
import software.momento.kotlin.sdk.responses.cache.GetResponse
import software.momento.kotlin.sdk.responses.cache.SetResponse
import java.io.Closeable
import kotlin.time.Duration

internal expect class InternalDataClient(credentialProvider: CredentialProvider, configuration: Configuration) : Closeable {

    internal suspend fun set(cacheName: String, key: String, value: String, ttl: Duration): SetResponse
    internal suspend fun get(cacheName: String, key: String): GetResponse
}