plugins {
    kotlin("jvm") version "1.9.22"
}

group = "software.momento.kotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.momento.kotlin:sdk:0.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    jvmToolchain(11)
}

tasks.register<JavaExec>("basic") {
    description = "Run the basic example"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "software.momento.example.BasicExampleKt"
}

tasks.register<JavaExec>("docExamples") {
    description = "Run the doc examples"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "software.momento.example.doc_examples.DocExamplesKt"
}

tasks.register<JavaExec>("readmeExample") {
    description = "Run the readme example"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "software.momento.example.doc_examples.ReadmeExampleKt"
}

tasks.register<JavaExec>("cheatSheetExample") {
    description = "Run the cheat sheet example"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "software.momento.example.doc_examples.CheatSheetKt"
}
