package software.momento.example

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import software.momento.kotlin.sdk.TopicClient
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.TopicConfigurations
import software.momento.kotlin.sdk.responses.topic.TopicMessage
import software.momento.kotlin.sdk.responses.topic.TopicPublishResponse
import software.momento.kotlin.sdk.responses.topic.TopicSubscribeResponse

private const val CACHE_NAME = "test-cache"
private const val TOPIC_NAME = "test-topic"

/**
 * Demonstrates how to handle discontinuities in a topic subscription stream.
 *
 * A discontinuity indicates that the subscriber missed one or more messages — for example,
 * because the connection was interrupted and the server's buffer was exceeded before reconnection.
 * When a discontinuity is received, the lastTopicSequence and newTopicSequence fields describe
 * the gap so you can decide how to recover (e.g. fetching missed state from a cache or database).
 */
fun main() = runBlocking {
    printStartBanner()

    TopicClient(
        CredentialProvider.fromEnvVarV2(), TopicConfigurations.Laptop.latest
    ).use { topicClient ->
        val subscribeResponse = topicClient.subscribe(CACHE_NAME, TOPIC_NAME)

        when (subscribeResponse) {
            is TopicSubscribeResponse.Error -> throw RuntimeException(
                "Failed to subscribe to topic '$TOPIC_NAME': ${subscribeResponse.errorCode}",
                subscribeResponse
            )

            is TopicSubscribeResponse.Subscription -> coroutineScope {
                // Publish a few messages in the background to drive the example
                launch {
                    delay(500)
                    repeat(5) { i ->
                        when (val response = topicClient.publish(CACHE_NAME, TOPIC_NAME, "message-$i")) {
                            is TopicPublishResponse.Success -> println("Published: message-$i")
                            is TopicPublishResponse.Error -> println("Publish error: ${response.errorCode}")
                        }
                        delay(200)
                    }
                }

                subscribeResponse.take(5).collect { message ->
                    when (message) {
                        is TopicMessage.Text -> println("Received text: ${message.value}")
                        is TopicMessage.Binary -> println("Received binary: ${message.value.size} bytes")
                        is TopicMessage.Discontinuity -> {
                            // Some messages between lastTopicSequence and newTopicSequence may have been dropped.
                            println(
                                "Subscriber received discontinuity, last sequence number ${message.lastTopicSequence}, new sequence number ${message.newTopicSequence}, and new sequence page: ${message.newSequencePage}"
                            )
                        }
                        is TopicMessage.Error -> throw RuntimeException(
                            "Error reading from topic '$TOPIC_NAME': ${message.errorCode}", message
                        )
                    }
                }
            }
        }
    }

    printEndBanner()
}

private fun printStartBanner() {
    println("******************************************************************")
    println("Topic Discontinuity Example Start")
    println("******************************************************************")
}

private fun printEndBanner() {
    println("******************************************************************")
    println("Topic Discontinuity Example End")
    println("******************************************************************")
}
