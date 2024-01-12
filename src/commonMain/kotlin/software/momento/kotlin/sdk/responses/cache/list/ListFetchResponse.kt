package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list fetch operation */
public sealed interface ListFetchResponse {

    /** A successful list fetch operation that found elements. */
    public data class Hit(val valueListByteArray: List<ByteArray>) : ListFetchResponse {

        /** Retrieves the values as a list of strings. */
        val valueListString: List<String> by lazy {
            valueListByteArray.map { String(it) }
        }
    }

    /** A successful list fetch operation that did not find elements. */
    public object Miss : ListFetchResponse

    /**
     * A failed list fetch operation. The response itself is an exception, and the message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListFetchResponse
}
