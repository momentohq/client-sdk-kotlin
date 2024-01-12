package software.momento.kotlin.sdk.internal

import com.google.protobuf.ByteString
import grpc.cache_client.ECacheResult
import grpc.cache_client._DeleteRequest
import grpc.cache_client._GetRequest
import grpc.cache_client._ListConcatenateBackRequest
import grpc.cache_client._ListConcatenateFrontRequest
import grpc.cache_client._ListFetchRequest
import grpc.cache_client._ListLengthRequest
import grpc.cache_client._ListPopBackRequest
import grpc.cache_client._ListPopFrontRequest
import grpc.cache_client._ListPushBackRequest
import grpc.cache_client._ListPushFrontRequest
import grpc.cache_client._ListRemoveRequest
import grpc.cache_client._ListRetainRequest
import grpc.cache_client._SetRequest
import software.momento.kotlin.sdk.internal.utils.ValidationUtils
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configuration
import software.momento.kotlin.sdk.exceptions.CacheServiceExceptionMapper
import software.momento.kotlin.sdk.exceptions.InternalServerException
import software.momento.kotlin.sdk.internal.utils.ByteStringExtensions.toByteString
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

internal actual class InternalDataClient actual constructor(
    credentialProvider: CredentialProvider, configuration: Configuration, private val itemDefaultTtl: Duration
) : InternalClient() {
    private val stubsManager: DataGrpcStubsManager

    init {
        stubsManager = DataGrpcStubsManager(credentialProvider, configuration.transportStrategy.grpcConfiguration)
    }

    internal actual suspend fun set(
        cacheName: String, key: String, value: String, ttl: Duration?
    ): SetResponse = validateAndSendSet(cacheName, key.toByteString(), value.toByteString(), ttl)

    internal actual suspend fun set(
        cacheName: String, key: ByteArray, value: String, ttl: Duration?
    ): SetResponse = validateAndSendSet(cacheName, key.toByteString(), value.toByteString(), ttl)

    internal actual suspend fun set(
        cacheName: String, key: String, value: ByteArray, ttl: Duration?
    ): SetResponse = validateAndSendSet(cacheName, key.toByteString(), value.toByteString(), ttl)

    internal actual suspend fun set(
        cacheName: String, key: ByteArray, value: ByteArray, ttl: Duration?
    ): SetResponse = validateAndSendSet(cacheName, key.toByteString(), value.toByteString(), ttl)

    private suspend fun validateAndSendSet(
        cacheName: String, key: ByteString, value: ByteString, ttl: Duration?
    ): SetResponse {
        val effectiveTtl = ttl ?: itemDefaultTtl
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTtl(effectiveTtl)
        }.fold(onSuccess = {
            sendSet(cacheName, key, value, effectiveTtl)
        }, onFailure = { e ->
            SetResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    private suspend fun sendSet(
        cacheName: String, key: ByteString, value: ByteString, ttl: Duration
    ): SetResponse {

        val request = _SetRequest.newBuilder().apply {
            cacheKey = key
            cacheBody = value
            ttlMilliseconds = ttl.inWholeMilliseconds
        }.build()

        val metadata = metadataWithCache(cacheName)

        return runCatching {
            stubsManager.stub.set(request, metadata)
        }.fold(onSuccess = {
            SetResponse.Success
        }, onFailure = { e ->
            SetResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
        })
    }

    internal actual suspend fun get(
        cacheName: String, key: String
    ): GetResponse = validateAndSendGet(cacheName, key.toByteString())

    internal actual suspend fun get(
        cacheName: String, key: ByteArray
    ): GetResponse = validateAndSendGet(cacheName, key.toByteString())

    private suspend fun validateAndSendGet(
        cacheName: String, key: ByteString
    ): GetResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
        }.fold(onSuccess = {
            sendGet(cacheName, key)
        }, onFailure = { e ->
            GetResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    private suspend fun sendGet(
        cacheName: String, key: ByteString
    ): GetResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _GetRequest.newBuilder().apply {
            cacheKey = key
        }.build()

        return runCatching {
            stubsManager.stub.get(request, metadata)
        }.fold(onSuccess = { getResponse ->
            when (getResponse.result) {
                ECacheResult.Hit -> GetResponse.Hit(getResponse.cacheBody.toByteArray())
                ECacheResult.Miss -> GetResponse.Miss
                else -> GetResponse.Error(InternalServerException("Unsupported cache Get result: ${getResponse.result}"))
            }
        }, onFailure = { e ->
            GetResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
        })
    }

    internal actual suspend fun delete(
        cacheName: String, key: String
    ): DeleteResponse = validateAndSendDelete(cacheName, key.toByteString())

    internal actual suspend fun delete(
        cacheName: String, key: ByteArray
    ): DeleteResponse = validateAndSendDelete(cacheName, key.toByteString())

    private suspend fun validateAndSendDelete(
        cacheName: String, key: ByteString
    ): DeleteResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
        }.fold(onSuccess = {
            sendDelete(cacheName, key)
        }, onFailure = { e ->
            DeleteResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    private suspend fun sendDelete(
        cacheName: String, key: ByteString
    ): DeleteResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _DeleteRequest.newBuilder().apply {
            cacheKey = key
        }.build()

        return runCatching {
            stubsManager.stub.delete(request, metadata)
        }.fold(onSuccess = {
            DeleteResponse.Success
        }, onFailure = { e ->
            DeleteResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
        })
    }


    internal actual suspend fun listConcatenateBack(
        cacheName: String,
        listName: String,
        values: Iterable<String>,
        truncateFrontToSize: Int?,
        ttl: CollectionTtl?
    ): ListConcatenateBackResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateFrontToSize)

            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListConcatenateBack(cacheName, listName.toByteString(), values.map { it.toByteString() }, truncateFrontToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListConcatenateBackResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    internal actual suspend fun listConcatenateBackByteArray(
        cacheName: String,
        listName: String,
        values: Iterable<ByteArray>,
        truncateFrontToSize: Int?,
        ttl: CollectionTtl?
    ): ListConcatenateBackResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateFrontToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListConcatenateBack(cacheName, listName.toByteString(), values.map { it.toByteString() }, truncateFrontToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListConcatenateBackResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListConcatenateBack(
        cacheName: String,
        listName: ByteString,
        values: List<ByteString>,
        truncateFrontToSize: Int?,
        ttl: CollectionTtl
    ): ListConcatenateBackResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListConcatenateBackRequest.newBuilder().apply {
            setListName(listName)
            addAllValues(values)
            ttlMilliseconds = ttl.toMilliseconds()!!
            refreshTtl = ttl.refreshTtl()
            truncateFrontToSize?.let { setTruncateFrontToSize(truncateFrontToSize) }
        }.build()

        return runCatching {
            stubsManager.stub.listConcatenateBack(request, metadata)
        }.fold(
            onSuccess = { response ->
                ListConcatenateBackResponse.Success(response.listLength)
            },
            onFailure = { e ->
                ListConcatenateBackResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listConcatenateFront(
        cacheName: String,
        listName: String,
        values: Iterable<String>,
        truncateBackToSize: Int?,
        ttl: CollectionTtl?
    ): ListConcatenateFrontResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateBackToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListConcatenateFront(cacheName, listName.toByteString(), values.map { it.toByteString() }, truncateBackToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListConcatenateFrontResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    internal actual suspend fun listConcatenateFrontByteArray(
        cacheName: String,
        listName: String,
        values: Iterable<ByteArray>,
        truncateBackToSize: Int?,
        ttl: CollectionTtl?
    ): ListConcatenateFrontResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateBackToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListConcatenateFront(cacheName, listName.toByteString(), values.map { it.toByteString() }, truncateBackToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListConcatenateFrontResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListConcatenateFront(
        cacheName: String,
        listName: ByteString,
        values: List<ByteString>,
        truncateBackToSize: Int?,
        ttl: CollectionTtl
    ): ListConcatenateFrontResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListConcatenateFrontRequest.newBuilder().apply {
            setListName(listName)
            addAllValues(values)
            ttlMilliseconds = ttl.toMilliseconds()!!
            refreshTtl = ttl.refreshTtl()
            truncateBackToSize?.let { setTruncateBackToSize(truncateBackToSize) }
        }.build()

        return runCatching {
            stubsManager.stub.listConcatenateFront(request, metadata)
        }.fold(
            onSuccess = { response ->
                ListConcatenateFrontResponse.Success(response.listLength)
            },
            onFailure = { e ->
                ListConcatenateFrontResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listFetch(
        cacheName: String,
        listName: String,
        startIndex: Int?,
        endIndex: Int?
    ): ListFetchResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireIndexRangeValid(startIndex, endIndex)
            sendListFetch(cacheName, listName.toByteString(), startIndex, endIndex)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListFetchResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListFetch(
        cacheName: String,
        listName: ByteString,
        startIndex: Int?,
        endIndex: Int?
    ) : ListFetchResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListFetchRequest.newBuilder().apply {
            setListName(listName)
            startIndex?.let { inclusiveStart = it }
            endIndex?.let { exclusiveEnd = it }
        }.build()

        return runCatching {
            stubsManager.stub.listFetch(request, metadata)
        }.fold(
            onSuccess = { response ->
                response.takeIf { it.hasFound() }
                    ?.run { ListFetchResponse.Hit(found.valuesList.map { it.toByteArray() }) }
                    ?: ListFetchResponse.Miss
            },
            onFailure = { e ->
                ListFetchResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listLength(
        cacheName: String,
        listName: String
    ): ListLengthResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            sendListLength(cacheName, listName.toByteString())
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListLengthResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListLength(  cacheName: String,
                                         listName: ByteString
    ): ListLengthResponse {

        val metadata = metadataWithCache(cacheName)

        val request = _ListLengthRequest.newBuilder().apply {
            setListName(listName)
        }.build()

        return runCatching {
            stubsManager.stub.listLength(request, metadata)
        }.fold(
            onSuccess = { response ->
                response.takeIf { it.hasFound() }
                    ?.run { ListLengthResponse.Hit(response.found.length) }
                    ?: ListLengthResponse.Miss
            },
            onFailure = { e ->
                ListLengthResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listPushFront(
        cacheName: String,
        listName: String,
        value: String,
        truncateBackToSize: Int?,
        ttl: CollectionTtl?
    ): ListPushFrontResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateBackToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListPushFront(cacheName, listName.toByteString(), value.toByteString(), truncateBackToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListPushFrontResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    internal actual suspend fun listPushFront(
        cacheName: String,
        listName: String,
        value: ByteArray,
        truncateBackToSize: Int?,
        ttl: CollectionTtl?
    ): ListPushFrontResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateBackToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListPushFront(cacheName, listName.toByteString(), value.toByteString(), truncateBackToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListPushFrontResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListPushFront(
        cacheName: String,
        listName: ByteString,
        value: ByteString,
        truncateBackToSize: Int?,
        ttl: CollectionTtl
    ): ListPushFrontResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListPushFrontRequest.newBuilder().apply {
            setListName(listName)
            setValue(value)
            ttlMilliseconds = ttl.toMilliseconds()!!
            refreshTtl = ttl.refreshTtl()
            truncateBackToSize?.let { setTruncateBackToSize(truncateBackToSize) }
        }.build()

        return runCatching {
            stubsManager.stub.listPushFront(request, metadata)
        }.fold(
            onSuccess = { response ->
                ListPushFrontResponse.Success(response.listLength)
            },
            onFailure = { e ->
                ListPushFrontResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listPushBack(
        cacheName: String,
        listName: String,
        value: String,
        truncateFrontToSize: Int?,
        ttl: CollectionTtl?
    ): ListPushBackResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateFrontToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListPushBack(cacheName, listName.toByteString(), value.toByteString(), truncateFrontToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListPushBackResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    internal actual suspend fun listPushBack(
        cacheName: String,
        listName: String,
        value: ByteArray,
        truncateFrontToSize: Int?,
        ttl: CollectionTtl?
    ): ListPushBackResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireValidTruncateToSize(truncateFrontToSize)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListPushBack(cacheName, listName.toByteString(), value.toByteString(), truncateFrontToSize, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListPushBackResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListPushBack(
        cacheName: String,
        listName: ByteString,
        value: ByteString,
        truncateFrontToSize: Int?,
        ttl: CollectionTtl
    ): ListPushBackResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListPushBackRequest.newBuilder().apply {
            setListName(listName)
            setValue(value)
            ttlMilliseconds = ttl.toMilliseconds()!!
            refreshTtl = ttl.refreshTtl()
            truncateFrontToSize?.let { setTruncateFrontToSize(truncateFrontToSize) }
        }.build()

        return runCatching {
            stubsManager.stub.listPushBack(request, metadata)
        }.fold(
            onSuccess = { response ->
                ListPushBackResponse.Success(response.listLength)
            },
            onFailure = { e ->
                ListPushBackResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listPopBack(
        cacheName: String,
        listName: String
    ): ListPopBackResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)

            sendListPopBack(cacheName, listName.toByteString())
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListPopBackResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListPopBack(
        cacheName: String,
        listName: ByteString
    ): ListPopBackResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListPopBackRequest.newBuilder().apply {
            setListName(listName)
        }.build()

        return runCatching {
            stubsManager.stub.listPopBack(request, metadata)
        }.fold(
            onSuccess = { response ->
                response.takeIf { it.hasFound() }
                    ?.run { ListPopBackResponse.Hit(response.found.back.toByteArray()) }
                    ?: ListPopBackResponse.Miss
            },
            onFailure = { e ->
                ListPopBackResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listPopFront(
        cacheName: String,
        listName: String
    ): ListPopFrontResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)

            sendListPopFront(cacheName, listName.toByteString())
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListPopFrontResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListPopFront(
        cacheName: String,
        listName: ByteString
    ): ListPopFrontResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListPopFrontRequest.newBuilder().apply {
            setListName(listName)
        }.build()

        return runCatching {
            stubsManager.stub.listPopFront(request, metadata)
        }.fold(
            onSuccess = { response ->
                response.takeIf { it.hasFound() }
                    ?.run { ListPopFrontResponse.Hit(response.found.front.toByteArray()) }
                    ?: ListPopFrontResponse.Miss
            },
            onFailure = { e ->
                ListPopFrontResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listRemoveValue(
        cacheName: String,
        listName: String,
        value: String
    ): ListRemoveValueResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)

            sendListRemoveValue(cacheName, listName.toByteString(), value.toByteString())
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListRemoveValueResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    internal actual suspend fun listRemoveValue(
        cacheName: String,
        listName: String,
        value: ByteArray
    ): ListRemoveValueResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)

            sendListRemoveValue(cacheName, listName.toByteString(), value.toByteString())
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListRemoveValueResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListRemoveValue(
        cacheName: String,
        listName: ByteString,
        value: ByteString
    ): ListRemoveValueResponse {
        val metadata = metadataWithCache(cacheName)

        val request = _ListRemoveRequest.newBuilder().apply {
            setListName(listName)
            allElementsWithValue = value
        }.build()

        return runCatching {
            stubsManager.stub.listRemove(request, metadata)
        }.fold(
            onSuccess = { _ ->
                ListRemoveValueResponse.Success
            },
            onFailure = { e ->
                ListRemoveValueResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }

    internal actual suspend fun listRetain(
        cacheName: String,
        listName: String,
        startIndex: Int?,
        endIndex: Int?,
        ttl: CollectionTtl?): ListRetainResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            ValidationUtils.requireIndexRangeValid(startIndex, endIndex)
            val effectiveTtl = ttl ?: CollectionTtl.of(itemDefaultTtl)

            sendListRetain(cacheName, listName.toByteString(), startIndex, endIndex, effectiveTtl)
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                ListRetainResponse.Error(CacheServiceExceptionMapper.convert(e))
            }
        )
    }

    private suspend fun sendListRetain(
        cacheName: String,
        listName: ByteString,
        startIndex: Int?,
        endIndex: Int?,
        ttl: CollectionTtl
    ): ListRetainResponse {

        val metadata = metadataWithCache(cacheName)

        val request = _ListRetainRequest.newBuilder().apply {
            setListName(listName)
            startIndex?.let { inclusiveStart = it }
            endIndex?.let { exclusiveEnd = it }
            ttlMilliseconds = ttl.toMilliseconds()!!
            refreshTtl = ttl.refreshTtl()
        }.build()

        return runCatching {
            stubsManager.stub.listRetain(request, metadata)
        }.fold(
            onSuccess = { _ ->
                ListRetainResponse.Success
            },
            onFailure = { e ->
                ListRetainResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
            }
        )
    }


    override fun close() {
        stubsManager.close()
    }
}
