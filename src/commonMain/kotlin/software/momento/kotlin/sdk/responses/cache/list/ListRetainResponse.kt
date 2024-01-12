package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list retain operation */
public sealed interface ListRetainResponse {
    /**
     * A successful list retain operation.
     */
    public object Success : ListRetainResponse

    /**
     * A failed list retain operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved. The message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException): SdkException(cause), ListRetainResponse
}