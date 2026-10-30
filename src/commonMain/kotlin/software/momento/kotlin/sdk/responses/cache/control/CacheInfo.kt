package software.momento.kotlin.sdk.responses.cache.control

/** Information about a cache.
 *  @param name the cache name
 *  @param id the cache id
 * */
public data class CacheInfo(public val name: String, public val id: String = "")
