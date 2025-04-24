package software.momento.kotlin.sdk.internal

import grpc.cache_client.pubsub.PubsubGrpcKt
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import software.momento.kotlin.sdk.auth.CredentialProvider
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes


/**
 * Manager responsible for GRPC channels and stubs for the Control Plane.
 *
 *
 * The business layer, will get request stubs from this layer. This keeps the two layers
 * independent and any future pooling of channels can happen exclusively in the manager without
 * impacting the API business logic.
 */
internal class TopicGrpcStubsManager(credentialProvider: CredentialProvider) : Closeable {
    private val channel: ManagedChannel
    private val futureStub: PubsubGrpcKt.PubsubCoroutineStub

    init {
        channel = setupConnection(credentialProvider)
        futureStub = PubsubGrpcKt.PubsubCoroutineStub(channel)
    }

    val unaryStub: PubsubGrpcKt.PubsubCoroutineStub
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

    val streamingStub: PubsubGrpcKt.PubsubCoroutineStub
        get() = futureStub

    override fun close() {
        channel.shutdown()
    }

    companion object {
        private val DEADLINE = 1.minutes
        private fun setupConnection(credentialProvider: CredentialProvider): ManagedChannel {
            val channelBuilder = ManagedChannelBuilder.forAddress(credentialProvider.cacheEndpoint, credentialProvider.getPort())
            val isSecure = credentialProvider.isSecure
            channelBuilder.disableRetry()

            if (isSecure == true) {
                channelBuilder.useTransportSecurity()
            } else {
                channelBuilder.usePlaintext()
            }

            channelBuilder.keepAliveTime(10, TimeUnit.SECONDS)
            channelBuilder.keepAliveTimeout(5, TimeUnit.SECONDS)
            channelBuilder.keepAliveWithoutCalls(true)
            val clientInterceptors: MutableList<ClientInterceptor> = ArrayList()
            clientInterceptors.add(UserHeaderInterceptor(credentialProvider.apiKey, "cache"))
            channelBuilder.intercept(clientInterceptors)
            return channelBuilder.build()
        }
    }
}

