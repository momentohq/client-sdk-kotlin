package software.momento.kotlin.sdk.internal

import grpc.cache_client.ScsGrpcKt
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import software.momento.kotlin.sdk.auth.CredentialProvider
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

internal class DataGrpcStubsManager(credentialProvider: CredentialProvider) : Closeable {
    private val channel: ManagedChannel
    private val futureStub: ScsGrpcKt.ScsCoroutineStub

    init {
        channel = setupConnection(credentialProvider)
        futureStub = ScsGrpcKt.ScsCoroutineStub(channel)
    }
    val stub: ScsGrpcKt.ScsCoroutineStub
        /**
         * Returns a stub with appropriate deadlines.
         *
         *
         * Each stub is deliberately decorated with Deadline. Deadlines work differently than timeouts.
         * When a deadline is set on a stub, it simply means that once the stub is created it must be used
         * before the deadline expires. Hence, the stub returned from here should never be cached and the
         * safest behavior is for clients to request a new stub each time.
         *
         *
         * [more information](https://github.com/grpc/grpc-java/issues/1495)
         */
        get() = futureStub.withDeadlineAfter(DEADLINE.inWholeSeconds, TimeUnit.SECONDS)

    override fun close() {
        channel.shutdown()
    }

    companion object {
        private val DEADLINE = 1.minutes
        private fun setupConnection(credentialProvider: CredentialProvider): ManagedChannel {
            val channelBuilder = ManagedChannelBuilder.forAddress(credentialProvider.cacheEndpoint, 443)
            channelBuilder.useTransportSecurity()
            channelBuilder.disableRetry()
            val clientInterceptors: MutableList<ClientInterceptor> = ArrayList()
            clientInterceptors.add(UserHeaderInterceptor(credentialProvider.apiKey))
            channelBuilder.intercept(clientInterceptors)
            return channelBuilder.build()
        }
    }
}