package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list pop back operation */
public sealed interface ListPopBackResponse {
    /** A successful list pop back operation. */
    public data class Hit(private val value: ByteArray) : ListPopBackResponse {

        /** Retrieves the value as a byte array. */
        public fun valueByteArray(): ByteArray = value

        /** Retrieves the value as a string. */
        public fun valueString(): String = String(value)

        /** Retrieves the value as a string. */
        public fun value() : String = valueString()
    }

    /** A successful pop back operation that did not find elements. */
    public object Miss : ListPopBackResponse

    /**
     * A failed pop back operation. The response itself is an exception, and the message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListPopBackResponse
}