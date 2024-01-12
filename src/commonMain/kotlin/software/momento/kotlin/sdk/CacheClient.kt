package software.momento.kotlin.sdk

import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configuration
import software.momento.kotlin.sdk.internal.InternalControlClient
import software.momento.kotlin.sdk.internal.InternalDataClient
import software.momento.kotlin.sdk.requests.CollectionTtl
import software.momento.kotlin.sdk.responses.cache.DeleteResponse
import software.momento.kotlin.sdk.responses.cache.GetResponse
import software.momento.kotlin.sdk.responses.cache.SetResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheDeleteResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheListResponse
import software.momento.kotlin.sdk.responses.cache.list.ListConcatenateBackResponse
import software.momento.kotlin.sdk.responses.cache.list.ListConcatenateFrontResponse
import software.momento.kotlin.sdk.responses.cache.list.ListFetchResponse
import software.momento.kotlin.sdk.responses.cache.list.ListLengthResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPopBackResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPopFrontResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPushBackResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPushFrontResponse
import software.momento.kotlin.sdk.responses.cache.list.ListRemoveValueResponse
import software.momento.kotlin.sdk.responses.cache.list.ListRetainResponse
import kotlin.time.Duration

/**
 * Client to perform operations against a Momento cache.
 * @param credentialProvider The provider for the credentials required to connect to Momento.
 * @param configuration The configuration object containing all tunable client settings.
 * @param itemDefaultTtl The default TTL for values written to a cache.
 */
public class CacheClient(
    credentialProvider: CredentialProvider,
    configuration: Configuration,
    itemDefaultTtl: Duration
) : AutoCloseable {

    private val dataClient: InternalDataClient
    private val controlClient: InternalControlClient

    init {
        dataClient = InternalDataClient(credentialProvider, configuration, itemDefaultTtl)
        controlClient = InternalControlClient(credentialProvider, configuration)
    }

    /**
     * Asynchronously creates a cache with the provided name.
     *
     * @param cacheName The name of the cache to be created.
     * @return The result of the cache creation: [CacheCreateResponse.Success] or [CacheCreateResponse.Error].
     */
    public suspend fun createCache(cacheName: String): CacheCreateResponse {
        return controlClient.createCache(cacheName)
    }

    /**
     * Asynchronously deletes a cache.
     *
     * @param cacheName The name of the cache to be deleted.
     * @return The result of the cache deletion: [CacheDeleteResponse.Success] or [CacheDeleteResponse.Error].
     */
    public suspend fun deleteCache(cacheName: String): CacheDeleteResponse {
        return controlClient.deleteCache(cacheName)
    }

    /**
     * Asynchronously lists all the caches in the account.
     *
     * @return The result of the cache list: [CacheListResponse.Success] or [CacheListResponse.Error].
     */
    public suspend fun listCaches(): CacheListResponse {
        return controlClient.listCaches()
    }

    /**
     * Asynchronously sets the value in cache with a given Time To Live (TTL) seconds.
     *
     * <p>If a value for this key is already present it will be replaced by the new value.
     *
     * @param cacheName The name of the cache to store the item in.
     * @param key The key under which the value is to be added.
     * @param value The value to be stored.
     * @param ttl The time to Live for the item in the cache. This TTL takes precedence over the TTL used when
     *     creating a cache client.
     * @return The result of the set operation: [SetResponse.Success] or [SetResponse.Error].
     */
    public suspend fun set(cacheName: String, key: String, value: String, ttl: Duration? = null) : SetResponse {
        return dataClient.set(cacheName, key, value, ttl)
    }

    /**
     * Asynchronously sets the value in cache with a given Time To Live (TTL) seconds.
     *
     * <p>If a value for this key is already present it will be replaced by the new value.
     *
     * @param cacheName The name of the cache to store the item in.
     * @param key The key under which the value is to be added.
     * @param value The value to be stored.
     * @param ttl The time to Live for the item in the cache. This TTL takes precedence over the TTL used when
     *     creating a cache client.
     * @return The result of the set operation: [SetResponse.Success] or [SetResponse.Error].
     */
    public suspend fun set(cacheName: String, key: ByteArray, value: String, ttl: Duration? = null) : SetResponse {
        return dataClient.set(cacheName, key, value, ttl)
    }

    /**
     * Asynchronously sets the value in cache with a given Time To Live (TTL) seconds.
     *
     * <p>If a value for this key is already present it will be replaced by the new value.
     *
     * @param cacheName The name of the cache to store the item in.
     * @param key The key under which the value is to be added.
     * @param value The value to be stored.
     * @param ttl The time to Live for the item in the cache. This TTL takes precedence over the TTL used when
     *     creating a cache client.
     * @return The result of the set operation: [SetResponse.Success] or [SetResponse.Error].
     */
    public suspend fun set(cacheName: String, key: String, value: ByteArray, ttl: Duration? = null) : SetResponse {
        return dataClient.set(cacheName, key, value, ttl)
    }

    /**
     * Asynchronously sets the value in cache with a given Time To Live (TTL) seconds.
     *
     * <p>If a value for this key is already present it will be replaced by the new value.
     *
     * @param cacheName The name of the cache to store the item in.
     * @param key The key under which the value is to be added.
     * @param value The value to be stored.
     * @param ttl The time to Live for the item in the cache. This TTL takes precedence over the TTL used when
     *     creating a cache client.
     * @return The result of the set operation: [SetResponse.Success] or [SetResponse.Error].
     */
    public suspend fun set(cacheName: String, key: ByteArray, value: ByteArray, ttl: Duration? = null) : SetResponse {
        return dataClient.set(cacheName, key, value, ttl)
    }

    /**
     * Asynchronously gets the cache value for the given key.
     *
     * @param cacheName The name of the cache to get the item from.
     * @param key The key to get.
     * @return The result of the get operation: [GetResponse.Hit] containing the value if an item is found,
     * [GetResponse.Miss] if the item is not found, or [GetResponse.Error].
     */
    public suspend fun get(cacheName: String, key: String) : GetResponse {
        return dataClient.get(cacheName, key)
    }

    /**
     * Asynchronously gets the cache value for the given key.
     *
     * @param cacheName The name of the cache to get the item from.
     * @param key The key to get.
     * @return The result of the get operation: [GetResponse.Hit] containing the value if an item is found,
     * [GetResponse.Miss] if the item is not found, or [GetResponse.Error].
     */
    public suspend fun get(cacheName: String, key: ByteArray) : GetResponse {
        return dataClient.get(cacheName, key)
    }

    /**
     * Asynchronously deletes the cache value for the given key.
     *
     * @param cacheName The name of the cache to delete the item in.
     * @param key The key to delete.
     * @return The result of the set operation: [DeleteResponse.Success] or [DeleteResponse.Error].
     */
    public suspend fun delete(cacheName: String, key: String) : DeleteResponse {
        return dataClient.delete(cacheName, key)
    }

    /**
     * Asynchronously deletes the cache value for the given key.
     *
     * @param cacheName The name of the cache to delete the item in.
     * @param key The key to delete.
     * @return The result of the set operation: [DeleteResponse.Success] or [DeleteResponse.Error].
     */
    public suspend fun delete(cacheName: String, key: ByteArray) : DeleteResponse {
        return dataClient.delete(cacheName, key)
    }

    /**
     * Appends a list of string values to the back of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to append to.
     * @param values The list of string values to append.
     * @param truncateFrontToSize Optional parameter to truncate the list from the front to a certain size after the append operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the concatenate back operation: [ListConcatenateBackResponse.Success] or [ListConcatenateBackResponse.Error].
     */
    public suspend fun listConcatenateBack(cacheName: String, listName: String, values: Iterable<String>, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null
    ): ListConcatenateBackResponse {
        return dataClient.listConcatenateBack(cacheName, listName, values, truncateFrontToSize, ttl)
    }

    /**
     * Appends a list of byte array values to the back of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to append to.
     * @param values The list of byte array values to append.
     * @param truncateFrontToSize Optional parameter to truncate the list from the front to a certain size after the append operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the concatenate back operation: [ListConcatenateBackResponse.Success] or [ListConcatenateBackResponse.Error].
     */
    public suspend fun listConcatenateBackByteArray(cacheName: String, listName: String, values: Iterable<ByteArray>, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null
    ): ListConcatenateBackResponse {
        return dataClient.listConcatenateBackByteArray(cacheName, listName, values, truncateFrontToSize, ttl)
    }

    /**
     * Prepends a list of string values to the front of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to prepend to.
     * @param values The list of string values to prepend.
     * @param truncateBackToSize Optional parameter to truncate the list from the back to a certain size after the prepend operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the concatenate front operation: [ListConcatenateFrontResponse.Success] or [ListConcatenateFrontResponse.Error].
     */
    public suspend fun listConcatenateFront(cacheName: String, listName: String, values: Iterable<String>, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null
    ): ListConcatenateFrontResponse {
        return dataClient.listConcatenateFront(cacheName, listName, values, truncateBackToSize, ttl)
    }

    /**
     * Prepends a list of byte array values to the front of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to prepend to.
     * @param values The list of byte array values to prepend.
     * @param truncateBackToSize Optional parameter to truncate the list from the back to a certain size after the prepend operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the concatenate front operation: [ListConcatenateFrontResponse.Success] or [ListConcatenateFrontResponse.Error].
     */
    public suspend fun listConcatenateFrontByteArray(cacheName: String, listName: String, values: Iterable<ByteArray>, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null
    ): ListConcatenateFrontResponse {
        return dataClient.listConcatenateFrontByteArray(cacheName, listName, values, truncateBackToSize, ttl)
    }

    /**
     * Retrieves a range of elements from an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to fetch from.
     * @param startIndex The starting index of the range (inclusive).
     * @param endIndex The ending index of the range (exclusive).
     * @return The result of the list fetch operation: [ListFetchResponse.Hit] or [ListFetchResponse.Miss] or [ListFetchResponse.Error].
     */
    public suspend fun listFetch(cacheName: String, listName: String, startIndex: Int? = null, endIndex: Int? = null): ListFetchResponse {
        return dataClient.listFetch(cacheName, listName, startIndex, endIndex)
    }

    /**
     * Retrieves the length of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to measure.
     * @return The result of the list length operation: [ListLengthResponse.Hit] or [ListLengthResponse.Miss] or [ListLengthResponse.Error].
     */
    public suspend fun listLength(cacheName: String, listName: String): ListLengthResponse {
        return dataClient.listLength(cacheName, listName)
    }

    /**
     * Adds a value to the back of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to append to.
     * @param value The value to add.
     * @param truncateFrontToSize Optional parameter to truncate the list from the front to a certain size after the append operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the push back operation: [ListPushBackResponse.Success] or [ListPushBackResponse.Error].
     */
    public suspend fun listPushBack(cacheName: String, listName: String, value: String, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null): ListPushBackResponse {
        return dataClient.listPushBack(cacheName, listName, value, truncateFrontToSize, ttl)
    }

    /**
     * Adds a value to the back of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to append to.
     * @param value The value to add.
     * @param truncateFrontToSize Optional parameter to truncate the list from the front to a certain size after the append operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the push back operation: [ListPushBackResponse.Success] or [ListPushBackResponse.Error].
     */
    public suspend fun listPushBack(cacheName: String, listName: String, value: ByteArray, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null): ListPushBackResponse {
        return dataClient.listPushBack(cacheName, listName, value, truncateFrontToSize, ttl)
    }

    /**
     * Adds a value to the front of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to prepend to.
     * @param value The value to add.
     * @param truncateBackToSize Optional parameter to truncate the list from the back to a certain size after the prepend operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the push front operation: [ListPushFrontResponse.Success] or [ListPushFrontResponse.Error].
     */
    public suspend fun listPushFront(cacheName: String, listName: String, value: String, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null): ListPushFrontResponse {
        return dataClient.listPushFront(cacheName, listName, value, truncateBackToSize, ttl)
    }

    /**
     * Adds a value to the front of an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to prepend to.
     * @param value The value to add.
     * @param truncateBackToSize Optional parameter to truncate the list from the back to a certain size after the prepend operation.
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the push front operation: [ListPushFrontResponse.Success] or [ListPushFrontResponse.Error].
     */
    public suspend fun listPushFront(cacheName: String, listName: String, value: ByteArray, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null): ListPushFrontResponse {
        return dataClient.listPushFront(cacheName, listName, value, truncateBackToSize, ttl)
    }

    /**
     * Removes the last value from an existing list in the cache and returns it.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to pop from.
     * @return The result of the pop back operation: [ListPopBackResponse.Hit] with the value if the list is not empty,
     * [ListPopBackResponse.Miss] if the list is empty, or [ListPopBackResponse.Error].
     */
    public suspend fun listPopBack(cacheName: String, listName: String): ListPopBackResponse {
        return dataClient.listPopBack(cacheName, listName)
    }

    /**
     * Removes the first value from an existing list in the cache and returns it.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to pop from.
     * @return The result of the pop front operation: [ListPopFrontResponse.Hit] with the value if the list is not empty,
     * [ListPopFrontResponse.Miss] if the list is empty, or [ListPopFrontResponse.Error].
     */
    public suspend fun listPopFront(cacheName: String, listName: String): ListPopFrontResponse {
        return dataClient.listPopFront(cacheName, listName)
    }

    /**
     * Removes a specific value from an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to remove the value from.
     * @param value The string value to be removed.
     * @return The result of the remove value operation: [ListRemoveValueResponse.Success] or [ListRemoveValueResponse.Error].
     */
    public suspend fun listRemoveValue(cacheName: String, listName: String, value: String): ListRemoveValueResponse {
        return dataClient.listRemoveValue(cacheName, listName, value)
    }

    /**
     * Removes a specific value from an existing list in the cache.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to remove the value from.
     * @param value The byte array value to be removed.
     * @return The result of the remove value operation: [ListRemoveValueResponse.Success] or [ListRemoveValueResponse.Error].
     */
    public suspend fun listRemoveValue(cacheName: String, listName: String, value: ByteArray): ListRemoveValueResponse {
        return dataClient.listRemoveValue(cacheName, listName, value)
    }

    /**
     * Retains a specific range of elements in an existing list in the cache by slicing it.
     *
     * @param cacheName The name of the cache containing the list.
     * @param listName The name of the list to retain elements in.
     * @param startIndex The starting index of the range to retain (inclusive).
     * @param endIndex The ending index of the range to retain (exclusive).
     * @param ttl Optional CollectionTtl for the list.
     * @return The result of the retain operation: [ListRetainResponse.Success] or [ListRetainResponse.Error].
     */
    public suspend fun listRetain(cacheName: String, listName: String, startIndex: Int? = null, endIndex: Int? = null, ttl: CollectionTtl? = null): ListRetainResponse {
        return dataClient.listRetain(cacheName, listName, startIndex, endIndex, ttl)
    }

    override fun close() {
        dataClient.close()
        controlClient.close()
    }
}
