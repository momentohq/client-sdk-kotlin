package software.momento.kotlin.sdk.responses.cache.control

import software.momento.kotlin.sdk.exceptions.SdkException

/** Response for a list caches operation.  */
public interface CacheListResponse {
    /** A successful list caches operation. Contains the discovered caches.  */
    public class Success(caches: List<CacheInfo>) :
        CacheListResponse {
        private val caches: List<CacheInfo>

        /**
         * Constructs a list caches success with a list of found caches.
         *
         * @param caches the retrieved caches.
         */
        init {
            this.caches = caches
        }

        /**
         * Gets a list of metadata about the caches.
         *
         * @return The metadata list.
         */
        public fun getCaches(): List<CacheInfo> {
            return caches
        }

        /**
         * {@inheritDoc}
         *
         *
         * Limits the caches to 5 to bound the size of the string.
         */
        override fun toString(): String {
            val limitedCaches = caches.asSequence()
                .map { it.name() }
                .take(5)
                .joinToString(separator = "\", \"", prefix = "\"", postfix = "\"...")
            return "${super.toString()}: $limitedCaches"
        }
    }

    /**
     * A failed list caches operation. The response itself is an exception, so it can be directly
     * thrown, or the cause of the error can be retrieved with [.getCause]. The message is a
     * copy of the message of the cause.
     */
    public class Error
    /**
     * Constructs a list caches error with a cause.
     *
     * @param cause the cause.
     */
        (cause: SdkException?) : SdkException(cause!!), CacheListResponse
}