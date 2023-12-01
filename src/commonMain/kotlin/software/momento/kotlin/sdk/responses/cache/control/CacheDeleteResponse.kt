package momento.sdk.responses.cache.control

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a delete cache operation  */
public interface CacheDeleteResponse {
    /** A successful delete cache operation.  */
    public class Success : CacheDeleteResponse

    /**
     * A failed delete cache operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved with [.getCause]. The message is a
     * copy of the message of the cause.
     */
    public class Error
    /**
     * Constructs a delete cache error with a cause.
     *
     * @param cause the cause.
     */
        (cause: SdkException?) : SdkException(cause!!), CacheDeleteResponse
}