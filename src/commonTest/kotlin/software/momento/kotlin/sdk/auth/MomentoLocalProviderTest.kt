package software.momento.kotlin.sdk.auth

import junit.framework.TestCase.assertEquals
import software.momento.kotlin.sdk.UsingTestRunner
import kotlin.test.Test

class MomentoLocalProviderTest : UsingTestRunner() {

    companion object {
        private const val DEFAULT_HOSTNAME = "127.0.0.1"
        private const val DEFAULT_PORT = 8080
    }

    @Test
    fun testDefaultMomentoLocalProvider() {
        val momentoLocalProvider = MomentoLocalProvider.create();
        assertEquals(momentoLocalProvider.controlEndpoint, DEFAULT_HOSTNAME);
        assertEquals(momentoLocalProvider.cacheEndpoint, DEFAULT_HOSTNAME);
        assertEquals(momentoLocalProvider.apiKey, "");
        assertEquals(momentoLocalProvider.isSecure, false);
        assertEquals(momentoLocalProvider.getPort(), DEFAULT_PORT);
    }

    @Test
    fun testMomentoLocalProvider_WithDifferentHostname() {
        val hostname = "momento.local";
        val momentoLocalProvider = MomentoLocalProvider.create(hostname);
        assertEquals(momentoLocalProvider.controlEndpoint, hostname);
        assertEquals(momentoLocalProvider.cacheEndpoint, hostname);
        assertEquals(momentoLocalProvider.apiKey, "");
        assertEquals(momentoLocalProvider.isSecure, false);
        assertEquals(momentoLocalProvider.getPort(), DEFAULT_PORT);
    }

    @Test
    fun testMomentoLocalProvider_WithDifferentPort() {
        val port = 9090;
        val momentoLocalProvider = MomentoLocalProvider.create(port = port);
        assertEquals(momentoLocalProvider.controlEndpoint, DEFAULT_HOSTNAME);
        assertEquals(momentoLocalProvider.cacheEndpoint, DEFAULT_HOSTNAME);
        assertEquals(momentoLocalProvider.apiKey, "");
        assertEquals(momentoLocalProvider.isSecure, false);
        assertEquals(momentoLocalProvider.getPort(), port);
    }
}

