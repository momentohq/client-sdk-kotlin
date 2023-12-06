package software.momento.kotlin.sdk.responses.cache.signing

import java.util.Date
import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a create signing key operation */
public interface SigningKeyCreateResponse {

    /** A successful create signing key operation. Contains the new signing key and metadata. */
   public class Success(
        private val keyId: String,
        private val endpoint: String,
        private val key: String,
        private val expiresAt: Date
    ) : SigningKeyCreateResponse {

        /**
         * Prints key metadata but not the key.
         */
        override fun toString(): String {
            return "${super.toString()}: keyId: \"$keyId\" endpoint: \"$endpoint\" expiresAt: \"$expiresAt\""
        }
    }

    /**
     * A failed signing key creation operation. The response itself is an exception, so it can be
     * directly thrown, or the cause of the error can be retrieved with [getCause]. The
     * message is a copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), SigningKeyCreateResponse
}
