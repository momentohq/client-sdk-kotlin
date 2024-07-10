package software.momento.kotlin.sdk.internal

internal class UserHeaderInterceptor(private val tokenValue: String, private val clientType: String) :
    io.grpc.ClientInterceptor {
    private var isUserAgentSent = false

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
                    val platformInfo = PlatformInfo()
                    metadata.put(SDK_AGENT_KEY, platformInfo.getSdkVersion(clientType))
                    metadata.put(RUNTIME_VERSION_KEY, platformInfo.runtimeVersion)
                    isUserAgentSent = true
                }
                super.start(listener, metadata)
            }
        }
    }

    companion object {
        private val AUTH_HEADER_KEY: io.grpc.Metadata.Key<String> =
            io.grpc.Metadata.Key.of("authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
        private val SDK_AGENT_KEY: io.grpc.Metadata.Key<String> =
            io.grpc.Metadata.Key.of("agent", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
        private val RUNTIME_VERSION_KEY: io.grpc.Metadata.Key<String> =
            io.grpc.Metadata.Key.of("runtime-version", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
    }
}

internal expect class PlatformInfo() {
    internal val runtimeVersion: String

    internal fun getSdkVersion(clientType: String): String
}
