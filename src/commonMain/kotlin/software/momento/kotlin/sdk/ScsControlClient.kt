package software.momento.kotlin.sdk

import com.google.common.util.concurrent.ListenableFuture
import grpc.control_client._Cache
import grpc.control_client._CreateCacheRequest;
import grpc.control_client._CreateCacheResponse;
import grpc.control_client._DeleteCacheRequest;
import grpc.control_client._DeleteCacheResponse;
import grpc.control_client._FlushCacheRequest;
import grpc.control_client._FlushCacheResponse;
import grpc.control_client._ListCachesRequest;
import grpc.control_client._ListCachesResponse;
import io.grpc.Metadata
import momento.sdk.ValidationUtils.checkCacheNameValid
import momento.sdk.exceptions.CacheServiceExceptionMapper.convert
import momento.sdk.responses.cache.control.CacheDeleteResponse
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheFlushResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheInfo
import software.momento.kotlin.sdk.responses.cache.control.CacheListResponse
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.annotation.Nonnull

/** Client for interacting with Scs Control Plane.  */
internal class ScsControlClient(@param:Nonnull private val credentialProvider: CredentialProvider) :
    ScsClient() {
    private val controlGrpcStubsManager: ScsControlGrpcStubsManager = ScsControlGrpcStubsManager(credentialProvider)

    fun createCache(cacheName: String): CompletableFuture<CacheCreateResponse> {
        return try {
            checkCacheNameValid(cacheName)
            sendCreateCache(cacheName)
        } catch (e: Exception) {
            CompletableFuture.completedFuture<CacheCreateResponse>(
                CacheCreateResponse.Error(convert(e))
            )
        }
    }

    fun deleteCache(cacheName: String): CompletableFuture<CacheDeleteResponse> {
        return try {
            checkCacheNameValid(cacheName)
            sendDeleteCache(cacheName)
        } catch (e: Exception) {
            CompletableFuture.completedFuture<CacheDeleteResponse>(
                CacheDeleteResponse.Error(convert(e))
            )
        }
    }

    fun flushCache(cacheName: String): CompletableFuture<CacheFlushResponse> {
        return try {
            checkCacheNameValid(cacheName)
            sendFlushCache(cacheName)
        } catch (e: Exception) {
            CompletableFuture.completedFuture<CacheFlushResponse>(
                CacheFlushResponse.Error(convert(e))
            )
        }
    }

    fun listCaches(): CompletableFuture<CacheListResponse> {
        return try {
            sendListCaches()
        } catch (e: Exception) {
            CompletableFuture.completedFuture(
                CacheListResponse.Error(convert(e))
            )
        }
    }

    private fun sendCreateCache(cacheName: String): CompletableFuture<CacheCreateResponse> {
        val metadata: Metadata = metadataWithCache(cacheName)
        val stubSupplier =
            Supplier<ListenableFuture<_CreateCacheResponse>> {
                attachMetadata(controlGrpcStubsManager.stub, metadata)
                    .createCache(_CreateCacheRequest.newBuilder().setCacheName(cacheName).build())
            }
        val success =
            Function<_CreateCacheResponse, CacheCreateResponse> { rsp: _CreateCacheResponse? -> CacheCreateResponse.Success() }
        val failure =
            Function<Throwable, CacheCreateResponse> { e: Throwable? ->
                CacheCreateResponse.Error(
                    convert(e, metadata)
                )
            }
        return executeGrpcFunction(stubSupplier, success, failure)
    }

    private fun sendDeleteCache(cacheName: String): CompletableFuture<CacheDeleteResponse> {
        val metadata: Metadata = metadataWithCache(cacheName)
        val stubSupplier =
            Supplier<ListenableFuture<_DeleteCacheResponse>> {
                attachMetadata(controlGrpcStubsManager.stub, metadata)
                    .deleteCache(_DeleteCacheRequest.newBuilder().setCacheName(cacheName).build())
            }
        val success =
            Function<_DeleteCacheResponse, CacheDeleteResponse> { rsp: _DeleteCacheResponse? -> CacheDeleteResponse.Success() }
        val failure =
            Function<Throwable, CacheDeleteResponse> { e: Throwable? ->
                CacheDeleteResponse.Error(
                    convert(e, metadata)
                )
            }
        return executeGrpcFunction(stubSupplier, success, failure)
    }

    private fun sendFlushCache(cacheName: String): CompletableFuture<CacheFlushResponse> {
        val metadata: Metadata = metadataWithCache(cacheName)
        val stubSupplier =
            Supplier<ListenableFuture<_FlushCacheResponse>> {
                attachMetadata(controlGrpcStubsManager.stub, metadata)
                    .flushCache(_FlushCacheRequest.newBuilder().setCacheName(cacheName).build())
            }
        val success =
            Function<_FlushCacheResponse, CacheFlushResponse> { rsp: _FlushCacheResponse? -> CacheFlushResponse.Success() }
        val failure =
            Function<Throwable, CacheFlushResponse> { e: Throwable? ->
                CacheFlushResponse.Error(
                    convert(e, metadata)
                )
            }
        return executeGrpcFunction(stubSupplier, success, failure)
    }

    private fun sendListCaches(): CompletableFuture<CacheListResponse> {
        val stubSupplier =
            Supplier<ListenableFuture<_ListCachesResponse>> {
                controlGrpcStubsManager
                    .stub
                    .listCaches(_ListCachesRequest.newBuilder().setNextToken("").build())
            }
        val success =
            Function<_ListCachesResponse, CacheListResponse> { rsp: _ListCachesResponse ->
                CacheListResponse.Success(
                    rsp.cacheList.stream()
                        .map { c: _Cache ->
                            CacheInfo(
                                c.cacheName
                            )
                        }
                        .collect(
                            Collectors.toList()
                        )
                )
            }
        val failure =
            Function<Throwable, CacheListResponse> { e: Throwable? ->
                CacheListResponse.Error(
                    convert(e)
                )
            }
        return executeGrpcFunction(stubSupplier, success, failure)
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}