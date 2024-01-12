package software.momento.kotlin.sdk.requests

import kotlin.time.Duration

/**
 * Represents the desired behavior for managing the TTL on collection objects (dictionaries, lists,
 * sets) in your cache.
 */
public data class CollectionTtl(private val ttl: Duration?, private val refreshTtl: Boolean) {

    /**
     * Converts the TTL to seconds if it is present.
     *
     * @return The TTL in seconds or null if the default TTL will be used.
     */
    public fun toSeconds(): Long? = ttl?.inWholeSeconds

    /**
     * Converts the TTL to milliseconds if it is present.
     *
     * @return The TTL in milliseconds or null if the default TTL will be used.
     */
    public fun toMilliseconds(): Long? = ttl?.inWholeMilliseconds

    /**
     * Returns whether the collection TTL will be refreshed.
     */
    public fun refreshTtl(): Boolean = refreshTtl

    public companion object {
        /**
         * The default way to handle TTLs for collections.
         */
        public fun fromCacheTtl(): CollectionTtl = CollectionTtl(null, true)

        /**
         * Constructs a CollectionTtl with the specified TTL.
         */
        public fun of(ttl: Duration?): CollectionTtl = CollectionTtl(ttl, true)

        /**
         * Constructs a CollectionTtl with the specified TTL. Will only refresh if the TTL is provided.
         */
        public fun refreshTtlIfProvided(ttl: Duration?): CollectionTtl = CollectionTtl(ttl, ttl != null)
    }

    /**
     * Copies the CollectionTtl, but it will refresh the TTL when the collection is modified.
     */
    public fun withRefreshTtlOnUpdates(): CollectionTtl = CollectionTtl(ttl, true)

    /**
     * Copies the CollectionTTL, but the TTL will not be refreshed when the collection is modified.
     */
    public fun withNoRefreshTtlOnUpdates(): CollectionTtl = CollectionTtl(ttl, false)
}
