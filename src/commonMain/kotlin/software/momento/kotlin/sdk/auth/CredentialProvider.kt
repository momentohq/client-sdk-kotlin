package software.momento.kotlin.sdk.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import software.momento.kotlin.sdk.internal.utils.decodeBase64

/**
 * Contains the information required for a Momento client to connect to and authenticate with
 * the Momento service.
 */
public data class CredentialProvider(
    val controlEndpoint: String, val cacheEndpoint: String, val apiKey: String
) {
    public companion object {
        /**
         * Creates a [CredentialProvider] from a Momento API key.
         * @param apiKey The Momento API key to use for authentication.
         * @param controlHost An optional override for the endpoint used for control operations.
         * @param cacheHost An optional override for the endpoint used for cache operations.
         */
        @Deprecated(message = "Use fromApiKeyV2() instead")
        public fun fromString(
            apiKey: String, controlHost: String? = null, cacheHost: String? = null
        ): CredentialProvider {
            if (isV2ApiKey(apiKey)) {
                throw InvalidArgumentException("Received a V2 API key. Are you using the correct key? Or did you mean to use"
              + "`fromApiKeyV2()` or `fromEnvVarV2()` instead?")
            }
            try {
                val provider = try {
                    processLegacyKey(apiKey)
                } catch (e: Exception) {
                    processV1Key(apiKey)
                }
                return provider.copy(
                    controlEndpoint = controlHost ?: provider.controlEndpoint,
                    cacheEndpoint = cacheHost ?: provider.cacheEndpoint
                )
            } catch (e: Exception) {
                throw InvalidArgumentException("Invalid API key")
            }
        }

        private fun isV2ApiKey(authToken: String): Boolean {
            return try {
                // JWT tokens have 3 parts separated by dots
                if (authToken.count { it == '.' } != 2) {
                    return false
                }
                
                // Decode the payload (second part)
                val parts = authToken.split('.')
                if (parts.size != 3) {
                    return false
                }
                
                val payload = parts[1].decodeBase64() ?: return false
                
                // Check if it contains "t":"g" (V2 key indicator)
                val json = Json.parseToJsonElement(payload).jsonObject
                return json["t"]?.jsonPrimitive?.content == "g"
            } catch (e: Exception) {
                false
            }
        }

        private fun isBase64Encoded(apiKey: String): Boolean {
            return try {
                 apiKey.decodeBase64()
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        /**
         * Creates a [CredentialProvider] using a V2 API key and endpoint.
         * @param apiKey The V2 API key to use for authentication.
         * @param endpoint The endpoint base domain (e.g., "cell-1-us-east-1-1.prod.a.momentohq.com").
         */
        public fun fromApiKeyV2(
            apiKey: String, endpoint: String
        ): CredentialProvider {
            if (apiKey.isBlank()) {
                throw InvalidArgumentException("Auth token string cannot be empty")
            }
            if (endpoint.isBlank()) {
                throw InvalidArgumentException("Endpoint string cannot be empty")
            }

            if (!isV2ApiKey(apiKey)) {
                throw InvalidArgumentException(
                    "Received an invalid V2 API key. Are you using the correct key? Or did you mean to use"
                        + "`fromString()` or `fromEnvVar()` instead?"
                )
            }
            return CredentialProvider(
                controlEndpoint = "control.$endpoint",
                cacheEndpoint = "cache.$endpoint",
                apiKey = apiKey
            )
        }

        /**
         * Creates a [CredentialProvider] using a V2 API key from an environment variable.
         * @param apiKeyEnvVar The name of the environment variable containing the V2 API key.
         * @param endpointEnvVar The name of the environment variable containing the endpoint.
         */
        public fun fromEnvVarV2(
            apiKeyEnvVar: String = "MOMENTO_API_KEY", endpointEnvVar: String = "MOMENTO_ENDPOINT"
        ): CredentialProvider {
            if (apiKeyEnvVar.isBlank()) {
                throw InvalidArgumentException("ApiKey env var name cannot be empty")
            }
            if (endpointEnvVar.isBlank()) {
                throw InvalidArgumentException("Endpoint env var name cannot be empty")
            }
            
            val apiKey = System.getenv(apiKeyEnvVar)
            if (apiKey.isNullOrBlank()) {
                throw InvalidArgumentException("Env var $apiKeyEnvVar must be set")
            }
            val endpoint = System.getenv(endpointEnvVar)
            if (endpoint.isNullOrBlank()) {
                throw InvalidArgumentException("Env var $endpointEnvVar must be set")
            }          
            return fromApiKeyV2(apiKey, endpoint)
        }

        /**
         * Creates a [CredentialProvider] from a disposable token.
         * @param disposableToken The disposable token to use for authentication.
         * @param controlHost An optional override for the endpoint used for control operations.
         * @param cacheHost An optional override for the endpoint used for cache operations.
         */
        public fun fromDisposableToken(
            disposableToken: String, controlHost: String? = null, cacheHost: String? = null
        ): CredentialProvider {
            return fromString(disposableToken, controlHost, cacheHost);
        }

        /**
         * Creates a [CredentialProvider] from a Momento API key stored in an environment variable.
         * @param envVar The name of the environment variable containing the Momento API key.
         * @param controlHost An optional override for the endpoint used for control operations.
         * @param cacheHost An optional override for the endpoint used for cache operations.
         */
        @Deprecated(message = "Use fromEnvVarV2() instead")
        public fun fromEnvVar(
            envVar: String, controlHost: String? = null, cacheHost: String? = null
        ): CredentialProvider {
            val apiKey = System.getenv(envVar) ?: throw InvalidArgumentException("Environment variable $envVar not set")
            return fromString(apiKey, controlHost, cacheHost)
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
}
