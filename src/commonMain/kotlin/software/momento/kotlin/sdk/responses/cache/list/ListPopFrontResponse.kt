package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list pop front operation */
public sealed interface ListPopFrontResponse {

    /** A successful list pop front operation. */
    public data class Hit(private val value: ByteArray) : ListPopFrontResponse {

        /** Retrieves the value as a byte array. */
        public fun valueByteArray(): ByteArray = value

        /** Retrieves the value as a string. */
        public fun valueString(): String = String(value)

        /** Retrieves the value as a string. */
        public fun value() : String = valueString()
    }

    /** A successful pop front operation that did not find elements. */
    public object Miss : ListPopFrontResponse

    /**
     * A failed pop front operation. The response itself is an exception, and the message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListPopFrontResponse
}