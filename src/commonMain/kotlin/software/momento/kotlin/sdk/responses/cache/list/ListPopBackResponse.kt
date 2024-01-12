package software.momento.kotlin.sdk.responses.cache.list

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list pop back operation */
public sealed interface ListPopBackResponse {
    /** A successful list pop back operation. */
    public data class Hit(val valueByteArray: ByteArray) : ListPopBackResponse {

        /** Retrieves the value as a string. */
        val valueString: String by lazy {
            String(valueByteArray)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Hit

            if (!valueByteArray.contentEquals(other.valueByteArray)) return false

            return true
        }

        override fun hashCode(): Int {
            return valueByteArray.contentHashCode()
        }
    }

    /** A successful pop back operation that did not find elements. */
    public object Miss : ListPopBackResponse

    /**
     * A failed pop back operation. The response itself is an exception, and the message is a
     * copy of the message of the cause.
     */
    public class Error(cause: SdkException) : SdkException(cause), ListPopBackResponse
}