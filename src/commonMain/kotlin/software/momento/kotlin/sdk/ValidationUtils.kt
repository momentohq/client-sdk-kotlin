package momento.sdk

import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import java.time.Duration

/**
 * Client-side validation methods. While we should rely on server for all validations, there are
 * some that cannot be delegated and instead fail in grpc client, like providing null inputs or a
 * negative ttl.
 */
public object ValidationUtils {
    internal const val REQUEST_DEADLINE_MUST_BE_POSITIVE = "Request deadline must be positive"
    internal const val CACHE_ITEM_TTL_CANNOT_BE_NEGATIVE = "Cache item TTL cannot be negative."
    internal const val A_NON_NULL_KEY_IS_REQUIRED = "A non-null key is required."
    internal const val A_NON_NULL_VALUE_IS_REQUIRED = "A non-null value is required."
    internal const val CACHE_NAME_IS_REQUIRED = "Cache name is required."
    internal const val DICTIONARY_NAME_IS_REQUIRED = "Dictionary name is required."
    internal const val SET_NAME_CANNOT_BE_NULL = "Set name cannot be null."
    internal const val SORTED_SET_NAME_CANNOT_BE_NULL = "Sorted set name cannot be null."
    internal const val LIST_NAME_CANNOT_BE_NULL = "List name cannot be null."
    internal const val INDEX_RANGE_INVALID =
        "endIndex (exclusive) must be larger than startIndex (inclusive)."
    internal const val SCORE_RANGE_INVALID =
        "maxScore (inclusive) must be greater than or equal to minScore (inclusive)."
    internal const val SIGNING_KEY_TTL_CANNOT_BE_NEGATIVE = "Signing key TTL cannot be negative."
    internal const val TRUNCATE_TO_SIZE_MUST_BE_POSITIVE = "Truncate-to-size must be positive"

    /**
     * Throws an [InvalidArgumentException] if the deadline is null or not positive.
     *
     * @param requestDeadline The deadline to validate.
     */
    public fun ensureRequestDeadlineValid(requestDeadline: Duration?) {
        if (requestDeadline == null || requestDeadline.isNegative || requestDeadline.isZero) {
            throw InvalidArgumentException(REQUEST_DEADLINE_MUST_BE_POSITIVE)
        }
    }

    internal fun checkCacheNameValid(cacheName: String?) {
        if (cacheName == null) {
            throw InvalidArgumentException(CACHE_NAME_IS_REQUIRED)
        }
    }

    internal fun checkDictionaryNameValid(dictionaryName: String?) {
        if (dictionaryName == null) {
            throw InvalidArgumentException(DICTIONARY_NAME_IS_REQUIRED)
        }
    }

    internal fun checkListNameValid(listName: String?) {
        if (listName == null) {
            throw InvalidArgumentException(LIST_NAME_CANNOT_BE_NULL)
        }
    }

    internal fun checkSetNameValid(setName: String?) {
        if (setName == null) {
            throw InvalidArgumentException(SET_NAME_CANNOT_BE_NULL)
        }
    }

    internal fun checkSortedSetNameValid(sortedSetName: String?) {
        if (sortedSetName == null) {
            throw InvalidArgumentException(SORTED_SET_NAME_CANNOT_BE_NULL)
        }
    }

    internal fun checkIndexRangeValid(startIndex: Int?, endIndex: Int?) {
        if (startIndex == null || endIndex == null) return
        if (endIndex <= startIndex) {
            throw InvalidArgumentException(INDEX_RANGE_INVALID)
        }
    }

    internal fun checkScoreRangeValid(minScore: Double?, maxScore: Double?) {
        if (minScore == null || maxScore == null) {
            return
        }
        if (maxScore < minScore) {
            throw InvalidArgumentException(SCORE_RANGE_INVALID)
        }
    }

    internal fun checkSortedSetOffsetValid(offset: Int?) {
        if (offset != null && offset < 0) {
            throw InvalidArgumentException("Offset must be greater than or equal to 0.")
        }
    }

    internal fun checkSortedSetCountValid(count: Int?) {
        if (count != null && count <= 0) {
            throw InvalidArgumentException("Count must be greater than 0.")
        }
    }

    internal fun ensureValidCacheSet(key: Any?, value: Any?, ttl: Duration) {
        ensureValidKey(key)
        ensureValidValue(value)
        ensureValidTtl(ttl)
    }

    internal fun ensureValidKey(key: Any?) {
        if (key == null) {
            throw InvalidArgumentException(A_NON_NULL_KEY_IS_REQUIRED)
        }
    }

    internal fun ensureValidValue(value: Any?) {
        if (value == null) {
            throw InvalidArgumentException(A_NON_NULL_VALUE_IS_REQUIRED)
        }
    }

    internal fun ensureValidTtl(ttl: Duration) {
        if (ttl.seconds < 0) {
            throw InvalidArgumentException(CACHE_ITEM_TTL_CANNOT_BE_NEGATIVE)
        }
    }

    internal fun ensureValidTtlMinutes(ttlMinutes: Duration) {
        if (ttlMinutes.toMinutes() < 0) {
            throw InvalidArgumentException(SIGNING_KEY_TTL_CANNOT_BE_NEGATIVE)
        }
    }

    internal fun ensureValidTruncateToSize(truncateToSize: Int?) {
        if (truncateToSize != null && truncateToSize <= 0) {
            throw InvalidArgumentException(TRUNCATE_TO_SIZE_MUST_BE_POSITIVE)
        }
    }
}