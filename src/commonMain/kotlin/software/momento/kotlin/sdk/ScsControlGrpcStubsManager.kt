package software.momento.kotlin.sdk

import grpc.control_client.ScsControlGrpc
import grpc.control_client.ScsControlGrpc.ScsControlFutureStub
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import software.momento.kotlin.sdk.auth.CredentialProvider
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

/**
 * Manager responsible for GRPC channels and stubs for the Control Plane.
 *
 *
 * The business layer, will get request stubs from this layer. This keeps the two layers
 * independent and any future pooling of channels can happen exclusively in the manager without
 * impacting the API business logic.
 */
internal class ScsControlGrpcStubsManager(@Nonnull credentialProvider: CredentialProvider) :
    Closeable {
    private val channel: ManagedChannel
    private val futureStub: ScsControlFutureStub

    init {
        channel = setupConnection(credentialProvider)
        futureStub = ScsControlGrpc.newFutureStub(channel)
    }

    val stub: ScsControlFutureStub
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
        get() = futureStub.withDeadlineAfter(DEADLINE.seconds, TimeUnit.SECONDS)

    override fun close() {
        channel.shutdown()
    }

    companion object {
        private val DEADLINE = Duration.ofMinutes(1)
        private fun setupConnection(credentialProvider: CredentialProvider): ManagedChannel {
            val channelBuilder =
                NettyChannelBuilder.forAddress(credentialProvider.controlEndpoint, 443)
            channelBuilder.useTransportSecurity()
            channelBuilder.disableRetry()
            val clientInterceptors: MutableList<ClientInterceptor> = ArrayList()
            channelBuilder.intercept(clientInterceptors)
            return channelBuilder.build()
        }
    }
}