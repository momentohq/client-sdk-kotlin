<head>
  <meta name="Momento Client Library Documentation for Kotlin" content="Momento client software development kit for Kotlin">
</head>
<img src="https://docs.momentohq.com/img/momento-logo-forest.svg" alt="logo" width="400"/>

[![project status](https://momentohq.github.io/standards-and-practices/badges/project-status-official.svg)](https://github.com/momentohq/standards-and-practices/blob/main/docs/momento-on-github.md)
[![project stability](https://momentohq.github.io/standards-and-practices/badges/project-stability-beta.svg)](https://github.com/momentohq/standards-and-practices/blob/main/docs/momento-on-github.md)

# Momento Client Library for Kotlin

Momento Cache is a fast, simple, pay-as-you-go caching solution without any of the operational overhead
required by traditional caching solutions.  This repo contains the source code for the Momento client library for Kotlin.

To get started with Momento you will need a Momento Auth Token. You can get one from the [Momento Console](https://console.gomomento.com).

* Website: [https://www.gomomento.com/](https://www.gomomento.com/)
* Momento Documentation: [https://docs.momentohq.com/](https://docs.momentohq.com/)
* Getting Started: [https://docs.momentohq.com/getting-started](https://docs.momentohq.com/getting-started)
* Momento SDK Documentation for Kotlin: [https://docs.momentohq.com/sdks/kotlin](https://docs.momentohq.com/sdks/kotlin)
* Discuss: [Momento Discord](https://discord.gg/3HkAKjUZGq)

## Packages

The Kotlin SDK is available on Maven Central:

### Gradle

```kotlin
dependencies {
    implementation("software.momento.kotlin:sdk:0.4.0")
}
```

### Maven

```xml
<dependency>
    <groupId>software.momento.kotlin</groupId>
    <artifactId>sdk</artifactId>
    <version>0.4.0</version>
</dependency>
```

## Usage

```kotlin
package software.momento.example.doc_examples

import kotlinx.coroutines.runBlocking
import software.momento.kotlin.sdk.CacheClient
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.responses.cache.GetResponse
import kotlin.time.Duration.Companion.seconds

fun main() = runBlocking {
    CacheClient(
        CredentialProvider.fromEnvVarV2(),
        Configurations.Laptop.latest,
        60.seconds
    ).use { client ->
        val cacheName = "cache"

        client.createCache(cacheName)

        client.set(cacheName, "key", "value")

        when (val response = client.get(cacheName, "key")) {
            is GetResponse.Hit -> println("Hit: ${response.value}")
            is GetResponse.Miss -> println("Miss")
            is GetResponse.Error -> throw response
        }
    }
}

```

## Getting Started and Documentation

Documentation is available on the [Momento Docs website](https://docs.momentohq.com/sdks/kotlin).

## Examples

Working example projects, with all required build configuration files, are available in the [examples](./examples) subdirectory.

## Developing

If you are interested in contributing to the SDK, please see the [CONTRIBUTING](./CONTRIBUTING.md) docs.

----------------------------------------------------------------------------------------
For more info, visit our website at [https://gomomento.com](https://gomomento.com)!
