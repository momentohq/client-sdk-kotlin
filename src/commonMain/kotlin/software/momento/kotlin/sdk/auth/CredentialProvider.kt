package software.momento.kotlin.sdk.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import software.momento.kotlin.sdk.internal.utils.decodeBase64
import java.net.URI

/**
 * Contains the information required for a Momento client to connect to and authenticate with
 * the Momento service.
 */
public data class CredentialProvider(
    val controlEndpoint: String, val cacheEndpoint: String, val apiKey: String, val isSecure: Boolean? = true, val port: Int? = 443
) {
    public companion object {
        /**
         * Creates a [CredentialProvider] from a Momento API key.
         * @param apiKey The Momento API key to use for authentication.
         * @param controlHost An optional override for the endpoint used for control operations.
         * @param cacheHost An optional override for the endpoint used for cache operations.
         */
        public fun fromString(
            apiKey: String, controlHost: String? = null, cacheHost: String? = null
        ): CredentialProvider {
            try {
                val provider = try {
                    processLegacyKey(apiKey)
                } catch (e: Exception) {
                    processV1Key(apiKey)
                }
                return provider.copy(
                    controlEndpoint = controlHost ?: provider.controlEndpoint,
                    cacheEndpoint = cacheHost ?: provider.cacheEndpoint,
                    isSecure = provider.isSecure,
                    port = provider.port
                )
            } catch (e: Exception) {
                throw InvalidArgumentException("Invalid API key")
            }
        }

        /**
         * Creates a [CredentialProvider] from a Momento API key stored in an environment variable.
         * @param envVar The name of the environment variable containing the Momento API key.
         * @param controlHost An optional override for the endpoint used for control operations.
         * @param cacheHost An optional override for the endpoint used for cache operations.
         */
        public fun fromEnvVar(
            envVar: String, controlHost: String? = null, cacheHost: String? = null
        ): CredentialProvider {
            val apiKey = System.getenv(envVar) ?: throw InvalidArgumentException("Environment variable $envVar not set")
            return fromString(apiKey, controlHost, cacheHost)
        }

        public fun forMomentoLocal(): CredentialProvider {
            return MomentoLocalProvider.create();
        }

        private val json: Json = Json { ignoreUnknownKeys = true }
        private fun processLegacyKey(apiKey: String): CredentialProvider {
            val keyParts = apiKey.split(".")
            if (keyParts.size != 3) {
                throw InvalidArgumentException("Malformed legacy API key")
            }

            val payloadJson = keyParts[1].decodeBase64() ?: throw InvalidArgumentException("Cannot decode base64")
            val payload = json.decodeFromString<LegacyKeyPayload>(payloadJson)

            return CredentialProvider(payload.controlEndpoint, payload.cacheEndpoint, apiKey)
        }

        private fun processV1Key(apiKey: String): CredentialProvider {
            val decodedString = apiKey.decodeBase64() ?: throw InvalidArgumentException("Cannot decode base64")
            val v1Key = json.decodeFromString<V1Key>(decodedString)

            return CredentialProvider("control.${v1Key.host}", "cache.${v1Key.host}", v1Key.apiKey)
        }

        @Serializable
        private data class V1Key(
            @SerialName("endpoint") val host: String, @SerialName("api_key") val apiKey: String
        )

        @Serializable
        private data class LegacyKeyPayload(
            @SerialName("cp") val controlEndpoint: String, @SerialName("c") val cacheEndpoint: String
        )
    }

    public fun getPort(): Int {
        return port ?: 443;
    }

}
