package software.momento.kotlin.sdk.internal

import grpc.control_client.ScsControlGrpcKt
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import software.momento.kotlin.sdk.auth.CredentialProvider
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

internal class ControlGrpcStubsManager(credentialProvider: CredentialProvider) : Closeable {
    private val channel: ManagedChannel
    private val futureStub: ScsControlGrpcKt.ScsControlCoroutineStub

    init {
        channel = setupConnection(credentialProvider)
        futureStub = ScsControlGrpcKt.ScsControlCoroutineStub(channel)
    }

    val stub: ScsControlGrpcKt.ScsControlCoroutineStub
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
            val channelBuilder = ManagedChannelBuilder.forAddress(credentialProvider.controlEndpoint, 443)
            channelBuilder.useTransportSecurity()
            channelBuilder.disableRetry()
            channelBuilder.disableServiceConfigLookUp();
            val clientInterceptors: MutableList<ClientInterceptor> = ArrayList()
            clientInterceptors.add(UserHeaderInterceptor(credentialProvider.apiKey, "cache"))
            channelBuilder.intercept(clientInterceptors)
            return channelBuilder.build()
        }
    }
}
