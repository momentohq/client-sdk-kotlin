package software.momento.kotlin.sdk.responses.topic

import software.momento.kotlin.sdk.exceptions.SdkException

/**
 * A message from a Momento Topic.
 */
public sealed interface TopicMessage {

    /**
     * A topic message containing a String value.
     *
     * @param value The String value.
     * @param tokenId The ID that was used to publish the message, or null if the token did not have an id.
     * This can be used to securely identify the sender of a message.
     */
    public data class Text(val value: String, val tokenId: String? = null) : TopicMessage

    /**
     * A topic message containing a ByteArray value.
     *
     * @param value The ByteArray value.
     * @param tokenId The ID that was used to publish the message, or null if the token did not have an id.
     * This can be used to securely identify the sender of a message.
     */
    public data class Binary(val value: ByteArray, val tokenId: String? = null) : TopicMessage {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Binary

            if (!value.contentEquals(other.value)) return false
            if (tokenId != other.tokenId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.contentHashCode()
            result = 31 * result + (tokenId?.hashCode() ?: 0)
            return result
        }
    }

    /**
     * An error encountered while subscribed to a topic. The message itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved.
     */
    public class Error(cause: SdkException) : SdkException(cause), TopicMessage
}
