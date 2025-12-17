pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "examples"

// Uncomment this below section to test examples against local sdk changes
// includeBuild("..") {
//    dependencySubstitution {
//        substitute(module("software.momento.kotlin:sdk"))
//    }
// }