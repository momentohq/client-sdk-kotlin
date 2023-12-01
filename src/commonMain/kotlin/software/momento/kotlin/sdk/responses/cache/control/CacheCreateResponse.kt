package software.momento.kotlin.sdk.responses.cache.control

import software.momento.kotlin.sdk.exceptions.SdkException

public interface CacheCreateResponse {

    /** A successful create cache operation.  */
    public class Success : CacheCreateResponse

    /**
     * A failed create cache operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved with [.getCause]. The message is a
     * copy of the message of the cause.
     */
    public class Error
    /**
     * Constructs a cache creation error with a cause.
     *
     * @param cause the cause.
     */
        (cause: SdkException?) : SdkException(cause!!), CacheCreateResponse
}