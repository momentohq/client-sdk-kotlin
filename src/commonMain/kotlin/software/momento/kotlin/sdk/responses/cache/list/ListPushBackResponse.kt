package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list push back operation */
public sealed interface ListPushBackResponse {

    /** A successful list push back operation. Contains the length of the list. */
    public data class Success(val listLength: Int): ListPushBackResponse

    /**
     * A failed list push back operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved. The message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListPushBackResponse
}