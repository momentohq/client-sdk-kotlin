package software.momento.kotlin.sdk.internal

import grpc.control_client._CreateCacheRequest
import grpc.control_client._DeleteCacheRequest
import grpc.control_client._ListCachesRequest
import io.grpc.Metadata
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configuration
import software.momento.kotlin.sdk.exceptions.CacheServiceExceptionMapper
import software.momento.kotlin.sdk.exceptions.MomentoErrorCode
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheDeleteResponse
import software.momento.kotlin.sdk.internal.utils.ValidationUtils
import software.momento.kotlin.sdk.responses.cache.control.CacheInfo
import software.momento.kotlin.sdk.responses.cache.control.CacheListResponse

internal actual class InternalControlClient actual constructor(
    credentialProvider: CredentialProvider, configuration: Configuration
) : InternalClient() {
    private val stubsManager: ControlGrpcStubsManager

    init {
        stubsManager = ControlGrpcStubsManager(credentialProvider)
    }

    internal actual suspend fun createCache(cacheName: String): CacheCreateResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
        }.fold(onSuccess = {
            sendCreateCache(cacheName)
        }, onFailure = { e ->
            CacheCreateResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    private suspend fun sendCreateCache(cacheName: String): CacheCreateResponse {
        val metadata = metadataWithCache(cacheName)
        val request = _CreateCacheRequest.newBuilder().setCacheName(cacheName).build()

        return runCatching {
            this.stubsManager.stub.createCache(request, metadata)
        }.fold(onSuccess = {
            CacheCreateResponse.Success
        }, onFailure = { e ->
            val sdkException = CacheServiceExceptionMapper.convert(e, metadata)
            when (sdkException.errorCode) {
                MomentoErrorCode.ALREADY_EXISTS_ERROR -> CacheCreateResponse.AlreadyExists
                else -> CacheCreateResponse.Error(sdkException)
            }
        })
    }

    internal actual suspend fun deleteCache(cacheName: String): CacheDeleteResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
        }.fold(onSuccess = {
            sendDeleteCache(cacheName)
        }, onFailure = { e ->
            CacheDeleteResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    internal actual suspend fun listCaches(): CacheListResponse {
        return runCatching {}.fold(onSuccess = {
            sendListCaches()
        }, onFailure = { e ->
            CacheListResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    private suspend fun sendListCaches(): CacheListResponse {
        val request = _ListCachesRequest.newBuilder().setNextToken("").build()
        val metadata = Metadata()

        return runCatching {
            this.stubsManager.stub.listCaches(request, metadata)
        }.fold(onSuccess = { listCacheResponse ->
            CacheListResponse.Success(
                listCacheResponse.cacheList.map { CacheInfo(it.cacheName, it.cacheId) }
            )
        }, onFailure = { e ->
            CacheListResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
        })
    }

    private suspend fun sendDeleteCache(
        cacheName: String
    ): CacheDeleteResponse {
        val metadata = metadataWithCache(cacheName)
        val request = _DeleteCacheRequest.newBuilder().setCacheName(cacheName).build()

        return runCatching {
            this.stubsManager.stub.deleteCache(request, metadata)
        }.fold(onSuccess = {
            CacheDeleteResponse.Success
        }, onFailure = { e ->
            CacheDeleteResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
        })
    }

    override fun close() {
        stubsManager.close()
    }
}
