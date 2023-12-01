package software.momento.kotlin.sdk.responses.cache.control

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a flush cache operation  */
public interface CacheFlushResponse {
    /** A successful flush cache operation.  */
    public class Success : CacheFlushResponse

    /**
     * A failed flush cache operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved with [.getCause]. The message is a
     * copy of the message of the cause.
     */
    public class Error
    /**
     * Constructs a cache flush error with a cause.
     *
     * @param cause the cause.
     */
        (cause: SdkException?) : SdkException(cause!!), CacheFlushResponse
}