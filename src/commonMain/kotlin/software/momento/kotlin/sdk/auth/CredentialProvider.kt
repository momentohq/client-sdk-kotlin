package software.momento.kotlin.sdk.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
        public fun fromString(
            apiKey: String, controlHost: String? = null, cacheHost: String? = null
        ): CredentialProvider {
            if (isGlobalApiKey(apiKey)) {
                throw InvalidArgumentException("Received a global API key. Are you using the correct key? Or did you mean to use"
              + "`globalKeyFromString()` or `globalKeyFromEnvVar()` instead?")
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

        private fun isGlobalApiKey(authToken: String): Boolean {
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
                
                // Check if it contains "t":"g" (global key indicator)
                return payload.contains("\"t\"") && payload.contains("\"g\"")
            } catch (e: Exception) {
                false
            }
        }

        private fun isBase64EncodedToken(apiKey: String): Boolean {
            // Check if it's a global JWT (which is allowed)
            if (isGlobalApiKey(apiKey)) {
                return false
            }
            
            // Legacy tokens have format: xxx.yyy.zzz (JWT format)
            if (apiKey.count { it == '.' } == 2) {
                return true
            }
            
            // Check if it's base64 encoded (V1 tokens are base64 encoded)
            return try {
                apiKey.decodeBase64() != null
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Creates a [CredentialProvider] using a global API key and endpoint.
         * @param apiKey The global API key to use for authentication.
         * @param endpoint The endpoint base domain (e.g., "cell-1-us-east-1.prod.a.momentohq.com").
         */
        public fun globalKeyFromString(
            apiKey: String, endpoint: String
        ): CredentialProvider {
            if (apiKey.isBlank()) {
                throw InvalidArgumentException("Auth token string cannot be empty")
            }
            if (endpoint.isBlank()) {
                throw InvalidArgumentException("Endpoint string cannot be empty")
            }

            if (isBase64EncodedToken(apiKey)) {
                throw InvalidArgumentException(
                    "Global API key appears to be a V1 or legacy token. Are you using the correct key? Or did you mean to use"
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
         * Creates a [CredentialProvider] using a global API key from an environment variable.
         * @param envVar The name of the environment variable containing the global API key.
         * @param endpoint The endpoint base domain (e.g., "cell-1-us-east-1.prod.a.momentohq.com").
         */
        public fun globalKeyFromEnvVar(
            envVar: String, endpoint: String
        ): CredentialProvider {
            if (envVar.isBlank()) {
                throw InvalidArgumentException("Env var name cannot be empty")
            }
            if (endpoint.isBlank()) {
                throw InvalidArgumentException("Endpoint string cannot be empty")
            }
            
            val apiKey = System.getenv(envVar)
            if (apiKey.isNullOrBlank()) {
                throw InvalidArgumentException("Env var $envVar must be set")
            }
            
            return globalKeyFromString(apiKey, endpoint)
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
