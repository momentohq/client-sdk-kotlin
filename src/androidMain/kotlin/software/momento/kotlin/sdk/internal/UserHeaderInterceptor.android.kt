package software.momento.kotlin.sdk.internal

import android.os.Build

internal actual class PlatformInfo {
    internal actual val sdkVersion: String
        get() {
            val version = this.javaClass.getPackage()?.implementationVersion ?:  "unknown"
            return "kotlin-android:$version"
        }
    internal actual val runtimeVersion: String
        get() {
            val kotlinVersion = KotlinVersion.CURRENT
            val androidVersion = Build.VERSION.RELEASE
            val deviceModel = Build.MODEL
            val manufacturer = Build.MANUFACTURER
            return "Android $androidVersion, Kotlin $kotlinVersion, $manufacturer $deviceModel"
        }
}
