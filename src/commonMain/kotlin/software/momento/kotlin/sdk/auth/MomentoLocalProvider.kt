package software.momento.kotlin.sdk.auth

/**
 * Provides a local stubbed CredentialProvider for development/testing with Momento Local.
 */
public object MomentoLocalProvider {
    private const val DEFAULT_HOSTNAME = "127.0.0.1"
    private const val DEFAULT_PORT = 8080

    @JvmStatic
    public fun create(
        hostname: String = DEFAULT_HOSTNAME,
        port: Int = DEFAULT_PORT
    ): CredentialProvider {
        return CredentialProvider(
            controlEndpoint = hostname,
            cacheEndpoint = hostname,
            apiKey = "",
            isSecure = false,
            port = port
        )
    }
}
