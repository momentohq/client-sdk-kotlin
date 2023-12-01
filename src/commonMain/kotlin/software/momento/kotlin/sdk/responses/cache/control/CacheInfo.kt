package software.momento.kotlin.sdk.responses.cache.control

/** Information about a cache.  */
public class CacheInfo
/**
 * Constructs a CacheInfo.
 *
 * @param name the name of the cache.
 */(private val name: String) {
    /**
     * Gets the name of the cache.
     *
     * @return the name.
     */
    public fun name(): String {
        return name
    }
}