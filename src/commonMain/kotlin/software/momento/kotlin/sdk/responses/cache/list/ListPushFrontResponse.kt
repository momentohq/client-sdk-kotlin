package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list push front operation */
public sealed interface ListPushFrontResponse {

    /** A successful list push front operation. Contains the length of the list. */
    public data class Success(val listLength: Int): ListPushFrontResponse

    /**
     * A failed list push front operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved. The message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListPushFrontResponse
}