package software.momento.kotlin.sdk

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.grpc.Metadata
import io.grpc.stub.AbstractFutureStub
import io.grpc.stub.MetadataUtils
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Supplier
import javax.annotation.Nonnull

internal abstract class ScsClient : Closeable {
    protected fun metadataWithCache(cacheName: String): Metadata {
        val metadata = Metadata()
        metadata.put(CACHE_NAME_KEY, cacheName)
        return metadata
    }

    protected fun <S : AbstractFutureStub<S>?> attachMetadata(stub: S, metadata: Metadata?): S {
        return stub!!.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
    }

    protected fun <SdkResponse, GrpcResponse> executeGrpcFunction(
        stubSupplier: Supplier<ListenableFuture<GrpcResponse>>,
        successFunction: Function<GrpcResponse, SdkResponse>,
        errorFunction: Function<Throwable?, SdkResponse>
    ): CompletableFuture<SdkResponse> {

        // Submit request to non-blocking stub
        val rspFuture = stubSupplier.get()

        // Build a CompletableFuture to return to caller
        val returnFuture: CompletableFuture<SdkResponse> =
            object : CompletableFuture<SdkResponse>() {
                override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                    // propagate cancel to the listenable future if called on returned completable future
                    val result = rspFuture.cancel(mayInterruptIfRunning)
                    super.cancel(mayInterruptIfRunning)
                    return result
                }
            }

        // Convert returned ListenableFuture to CompletableFuture
        Futures.addCallback(
            rspFuture,
            object : FutureCallback<GrpcResponse> {
                override fun onSuccess(rsp: GrpcResponse) {
                    returnFuture.complete(successFunction.apply(rsp))
                }

                override fun onFailure(@Nonnull e: Throwable) {
                    returnFuture.complete(errorFunction.apply(e))
                }
            },  // Execute on same thread that called execute on CompletionStage
            MoreExecutors.directExecutor()
        )
        return returnFuture
    }

    companion object {
        private val CACHE_NAME_KEY = Metadata.Key.of("cache", Metadata.ASCII_STRING_MARSHALLER)
    }
}