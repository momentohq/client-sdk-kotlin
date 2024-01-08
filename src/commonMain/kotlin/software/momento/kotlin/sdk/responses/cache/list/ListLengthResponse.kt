package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list length operation */
public sealed interface ListLengthResponse {

    /** A successful list length operation containing the length of the list */
    public data class Hit(public val listLength: Int) : ListLengthResponse

    /** A successful list length operation that did not find elements. */
    public object Miss : ListLengthResponse

    /**
     * A failed list length operation. The response itself is an exception, and the message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListLengthResponse
}