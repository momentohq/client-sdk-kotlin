package software.momento.kotlin.sdk.internal

import kotlin.jvm.Volatile

internal class UserHeaderInterceptor(sdkType: String, private val tokenValue: String) : io.grpc.ClientInterceptor {
    private val sdkVersion = String.format("kotlin-$sdkType:%s", this.javaClass.getPackage()?.implementationVersion ?:  "unknown")
    override fun <ReqT, RespT> interceptCall(
        methodDescriptor: io.grpc.MethodDescriptor<ReqT, RespT>,
        callOptions: io.grpc.CallOptions,
        channel: io.grpc.Channel
    ): io.grpc.ClientCall<ReqT, RespT> {
        return object : io.grpc.ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(methodDescriptor, callOptions)
        ) {
            override fun start(listener: Listener<RespT>, metadata: io.grpc.Metadata) {
                metadata.put(AUTH_HEADER_KEY, tokenValue)
                if (!isUserAgentSent) {
                    metadata.put(SDK_AGENT_KEY, sdkVersion)
                    isUserAgentSent = true
                }
                super.start(listener, metadata)
            }
        }
    }

    companion object {
        private val AUTH_HEADER_KEY: io.grpc.Metadata.Key<String> =
            io.grpc.Metadata.Key.of("Authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
        private val SDK_AGENT_KEY: io.grpc.Metadata.Key<String> =
            io.grpc.Metadata.Key.of("Agent", io.grpc.Metadata.ASCII_STRING_MARSHALLER)

        @Volatile
        private var isUserAgentSent = false
    }
}

