package software.momento.kotlin.sdk.responses.cache.signing

import software.momento.kotlin.sdk.exceptions.SdkException


/** Response for a revoke signing key operation. */
public interface SigningKeyRevokeResponse {

    /** A successful revoke signing key operation. */
    public class Success : SigningKeyRevokeResponse

    /**
     * A failed revoke signing key operation. The response itself is an exception, so it can be
     * directly thrown, or the cause of the error can be retrieved with [cause]. The
     * message is a copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), SigningKeyRevokeResponse
}
