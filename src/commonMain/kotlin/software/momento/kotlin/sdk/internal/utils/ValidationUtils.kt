package software.momento.kotlin.sdk.internal.utils

import kotlin.time.Duration

/**
 * Client-side validation methods. While we should rely on server for all validations, there are
 * some that cannot be delegated and instead fail in grpc client, like providing a negative ttl.
 */
public object ValidationUtils {
    private const val CACHE_ITEM_TTL_MUST_BE_POSITIVE = "Cache item TTL must be positive"
    private const val CACHE_NAME_IS_REQUIRED = "Non-empty cache name is required"
    private const val LIST_NAME_IS_REQUIRED = "List name is required and cannot be null"
    private const val TRUNCATE_TO_SIZE_MUST_BE_POSITIVE = "Truncate to size must be positive"
    private const val A_NON_NULL_VALUE_IS_REQUIRED = "A non-null value is required"
    private const val INDEX_RANGE_INVALID = "End index must be greater than start index"

    internal fun requireValidCacheName(cacheName: String) {
        require(cacheName.isNotBlank()) { CACHE_NAME_IS_REQUIRED }
    }

    internal fun requireValidTtl(ttl: Duration) {
        require(ttl.isPositive()) { CACHE_ITEM_TTL_MUST_BE_POSITIVE }
    }

    internal fun requireValidTruncateToSize(truncateToSize: Int?) {
        require(truncateToSize == null || truncateToSize > 0) { TRUNCATE_TO_SIZE_MUST_BE_POSITIVE }
    }

    internal fun requireValidListName(listName: String?) {
        require(!listName.isNullOrBlank()) { LIST_NAME_IS_REQUIRED }
    }

    internal fun requireValidValue(value: Any?) {
        require(value != null) { A_NON_NULL_VALUE_IS_REQUIRED }
    }

    internal fun requireIndexRangeValid(startIndex: Int?, endIndex: Int?) {
        if (startIndex == null || endIndex == null) return

        require(endIndex > startIndex) { INDEX_RANGE_INVALID }
    }
}
