plugins {
    kotlin("multiplatform") version "1.9.21"
    id("com.android.library") version "8.1.4"
    kotlin("plugin.serialization") version "1.9.21"
}

repositories {
    mavenCentral()
    google()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

android {
    namespace = "software.momento.kotlin"

    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}

kotlin {
    explicitApi()
    androidTarget()
    jvm()
    jvmToolchain(11)
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
                implementation("software.momento.kotlin:client-protos-jvm:0.1.0-SNAPSHOT")
                implementation("com.google.code.gson:gson:2.10.1")
                implementation("io.grpc:grpc-kotlin-stub:1.4.1")
                implementation("io.grpc:grpc-api:1.59.1")
                implementation("io.grpc:grpc-context:1.59.1")
                implementation("io.grpc:grpc-protobuf:1.59.1")
                implementation("io.grpc:grpc-netty-shaded:1.59.1")
                implementation("io.grpc:grpc-stub:1.59.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                // JVM-specific dependencies
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val androidMain by getting {
            dependencies {
                // Android-specific dependencies
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.robolectric:robolectric:4.11.1")
            }
        }
    }
}
