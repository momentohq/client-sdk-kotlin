package software.momento.kotlin.sdk.responses.cache.signing

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list signing keys operation. */
public interface SigningKeyListResponse {

    /** A successful list signing keys operation. Contains the discovered signing keys. */
    public class Success(private val signingKeys: List<SigningKey>) : SigningKeyListResponse {

        /**
         * Returns the retrieved signing keys.
         *
         * @return the keys.
         */
        public fun signingKeys(): List<SigningKey> = signingKeys

        /**
         * Limits the keys to 5 to bound the size of the string. Prints the key ids instead of the
         * keys.
         */
        override fun toString(): String =
            "${super.toString()}: keys: " +
                    signingKeys.take(5).joinToString(separator = "\", \"", prefix = "\"", postfix = "\"...") { it.keyId }
    }

    /**
     * A failed list signing keys operation. The response itself is an exception, so it can be
     * directly thrown, or the cause of the error can be retrieved with [getCause]. The
     * message is a copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), SigningKeyListResponse
}
