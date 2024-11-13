import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("multiplatform") version "1.9.21"
    id("com.android.library") version "8.1.4"
    kotlin("plugin.serialization") version "1.9.21"
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    signing
}

group = "software.momento.kotlin"
// x-release-please-start-version
version = "0.2.2-SNAPSHOT"
// x-release-please-end

repositories {
    mavenCentral()
    google()
}

android {
    namespace = "software.momento.kotlin.sdk"

    compileSdk = 34
    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["MomentoApiKey"] = System.getenv("MOMENTO_API_KEY") ?: "noApiKeySet"
        testInstrumentationRunnerArguments["TestCacheName"] = System.getenv("TEST_CACHE_NAME") ?: "test-android-cache"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    explicitApi()
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm()
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("io.grpc:grpc-api:1.57.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("software.momento.kotlin:client-protos-jvm:0.119.2")
                runtimeOnly("io.grpc:grpc-netty:1.57.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("software.momento.kotlin:client-protos-android:0.119.2")
                runtimeOnly("io.grpc:grpc-okhttp:1.57.2")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.robolectric:robolectric:4.11.1")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                implementation("androidx.test.ext:junit:1.2.1")
                implementation("androidx.test.espresso:espresso-core:3.6.1")
            }
        }
    }

    targets.forEach {
        it.compilations.all {
            kotlinOptions {
                freeCompilerArgs += "-Xexpect-actual-classes"
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.apiVersion.set(KotlinVersion.KOTLIN_1_6)
    compilerOptions.languageVersion.set(KotlinVersion.KOTLIN_1_6)
}

val javadocJar = tasks.register("javadocJar", Jar::class.java) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("Momento Kotlin SDK")
                description.set("Kotlin client SDK for Momento Serverless Cache")
                url.set("https://github.com/momentohq/client-sdk-kotlin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("momento")
                        name.set("Momento")
                        organization.set("Momento")
                        email.set("eng-deveco@momentohq.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/momentohq/client-sdk-kotlin.git")
                    developerConnection.set("scm:git:git@github.com:momentohq/client-sdk-kotlin.git")
                    url.set("https://github.com/momentohq/client-sdk-kotlin")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

// Sign only if we have a key to do so
val signingKey: String? = System.getenv("SONATYPE_SIGNING_KEY")
if (signingKey != null) {
    signing {
        useInMemoryPgpKeys(signingKey, System.getenv("SONATYPE_SIGNING_KEY_PASSWORD"))
        sign(publishing.publications)
    }
}

// Fix Gradle error about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
