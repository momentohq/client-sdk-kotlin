{{ ossHeader }}

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
{% include "./examples/src/main/kotlin/software/momento/example/doc_examples/ReadmeExample.kt" %}
```

## Getting Started and Documentation

Documentation is available on the [Momento Docs website](https://docs.momentohq.com/sdks/kotlin).

## Examples

Working example projects, with all required build configuration files, are available in the [examples](./examples) subdirectory.

## Developing

If you are interested in contributing to the SDK, please see the [CONTRIBUTING](./CONTRIBUTING.md) docs.

{{ ossFooter }}
