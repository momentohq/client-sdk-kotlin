package software.momento.kotlin.sdk.internal

internal actual class PlatformInfo {
    internal actual val runtimeVersion: String
        get() {
            val javaVendor = System.getProperty("java.vendor")
            val javaVersion = System.getProperty("java.version")
            val kotlinVersion = KotlinVersion.CURRENT
            return "$javaVendor:$javaVersion, Kotlin $kotlinVersion"
        }

    internal actual fun getSdkVersion(clientType: String): String {
        val version = this.javaClass.getPackage()?.implementationVersion ?: "unknown"
        return "kotlin-jvm:$clientType:$version"
    }
}
