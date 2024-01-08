package software.momento.kotlin.sdk.internal

import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configuration
import software.momento.kotlin.sdk.requests.CollectionTtl
import software.momento.kotlin.sdk.responses.cache.DeleteResponse
import software.momento.kotlin.sdk.responses.cache.GetResponse
import software.momento.kotlin.sdk.responses.cache.SetResponse
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

internal expect class InternalDataClient(
    credentialProvider: CredentialProvider,
    configuration: Configuration,
    itemDefaultTtl: Duration
) : InternalClient {

    internal suspend fun set(cacheName: String, key: String, value: String, ttl: Duration? = null): SetResponse

    internal suspend fun set(cacheName: String, key: ByteArray, value: String, ttl: Duration? = null): SetResponse

    internal suspend fun set(cacheName: String, key: String, value: ByteArray, ttl: Duration? = null): SetResponse

    internal suspend fun set(cacheName: String, key: ByteArray, value: ByteArray, ttl: Duration? = null): SetResponse

    internal suspend fun get(cacheName: String, key: String): GetResponse

    internal suspend fun get(cacheName: String, key: ByteArray): GetResponse

    internal suspend fun delete(cacheName: String, key: String): DeleteResponse

    internal suspend fun delete(cacheName: String, key: ByteArray): DeleteResponse

    internal suspend fun listConcatenateBack(cacheName: String, listName: String, values: Iterable<String>, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null): ListConcatenateBackResponse

    internal suspend fun listConcatenateBackByteArray(cacheName: String, listName: String, values: Iterable<ByteArray>, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null): ListConcatenateBackResponse

    internal suspend fun listConcatenateFront(cacheName: String, listName: String, values: Iterable<String>, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null): ListConcatenateFrontResponse

    internal suspend fun listConcatenateFrontByteArray(cacheName: String, listName: String, values: Iterable<ByteArray>, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null): ListConcatenateFrontResponse

    internal suspend fun listFetch(cacheName: String, listName: String, startIndex: Int?, endIndex: Int?): ListFetchResponse

    internal suspend fun listLength(cacheName: String, listName: String): ListLengthResponse

    internal suspend fun listPushBack(cacheName: String, listName: String, value: String, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null): ListPushBackResponse

    internal suspend fun listPushBack(cacheName: String, listName: String, value: ByteArray, truncateFrontToSize: Int? = null, ttl: CollectionTtl? = null): ListPushBackResponse

    internal suspend fun listPushFront(cacheName: String, listName: String, value: String, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null): ListPushFrontResponse

    internal suspend fun listPushFront(cacheName: String, listName: String, value: ByteArray, truncateBackToSize: Int? = null, ttl: CollectionTtl? = null): ListPushFrontResponse

    internal suspend fun listPopBack(cacheName: String, listName: String): ListPopBackResponse

    internal suspend fun listPopFront(cacheName: String, listName: String): ListPopFrontResponse

    internal suspend fun listRemoveValue(cacheName: String, listName: String, value: String): ListRemoveValueResponse

    internal suspend fun listRemoveValue(cacheName: String, listName: String, value: ByteArray): ListRemoveValueResponse

    internal suspend fun listRetain(cacheName: String, listName: String, startIndex: Int?, endIndex: Int?, ttl: CollectionTtl? = null): ListRetainResponse
}
