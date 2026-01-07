package software.momento.kotlin.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.TopicConfigurations
import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import software.momento.kotlin.sdk.responses.topic.TopicMessage
import software.momento.kotlin.sdk.responses.topic.TopicPublishResponse
import software.momento.kotlin.sdk.responses.topic.TopicSubscribeResponse
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

class TopicClientTest : BaseJvmTestClass() {

    companion object {
        private lateinit var topicClient: TopicClient

        @JvmStatic
        @BeforeClass
        fun setUp() {
            topicClient = TopicClient(
                credentialProvider = CredentialProvider.fromEnvVar("V1_API_KEY"),
                configuration = TopicConfigurations.Laptop.latest
            )
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            topicClient.close()
        }
    }

    @Test
    fun publishFailsWithInvalidCacheName_String() = runTest {
        val response = topicClient.publish("", "topic", "value")
        assert((response as TopicPublishResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun publishFailsWithInvalidCacheName_ByteArray() = runTest {
        val response = topicClient.publish("", "topic", "value".encodeToByteArray())
        assert((response as TopicPublishResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun subscribeFailsWithInvalidCacheName_String() = runTest {
        val response = topicClient.subscribe("", "topic")
        assert((response as TopicSubscribeResponse.Error).cause is InvalidArgumentException)
    }

    @Test(timeout = 20_000)
    fun publishSubscribeHappyPath_String() = runBlocking {
        val topicName = "happyPathString"

        val valuesToSend = listOf("one", "two", "three", "four", "five")

        val messageFlow = topicClient.subscribe(cacheName, topicName)
        assert(messageFlow is TopicSubscribeResponse.Subscription)

        launch {
            delay(2000)

            for (value in valuesToSend) {
                val publishResponse = topicClient.publish(cacheName, topicName, value)
                assert(publishResponse is TopicPublishResponse.Success)
                delay(100)
            }
        }

        val receivedStrings = (messageFlow as TopicSubscribeResponse.Subscription)
            .take(valuesToSend.size)
            .toCollection(mutableListOf())
            .map { it as TopicMessage.Text }
            .map { it.value }
        assertNotNull(receivedStrings)

        assertContentEquals(valuesToSend, receivedStrings)
    }

    @Test(timeout = 20_000)
    fun publishSubscribeHappyPath_Bytes() = runBlocking {
        val topicName = "happyPathBytes"

        val valuesToSend = listOf(
            "one".encodeToByteArray(),
            "two".encodeToByteArray(),
            "three".encodeToByteArray(),
            "four".encodeToByteArray(),
            "five".encodeToByteArray()
        )

        val messageFlow = topicClient.subscribe(cacheName, topicName)
        assert(messageFlow is TopicSubscribeResponse.Subscription)


        launch {
            delay(2000)

            for (value in valuesToSend) {
                val publishResponse = topicClient.publish(cacheName, topicName, value)
                assert(publishResponse is TopicPublishResponse.Success)
                delay(100)
            }
        }

        val receivedByteArrays = (messageFlow as TopicSubscribeResponse.Subscription)
            .take(valuesToSend.size)
            .toCollection(mutableListOf())
            .map { it as TopicMessage.Binary }
            .map { it.value }
        assertNotNull(receivedByteArrays)

        for (i in valuesToSend.indices) {
            assertContentEquals(valuesToSend[i], receivedByteArrays[i])
        }
    }

    @Test(timeout = 20_000)
    fun publishMultipleSubscriptionsHappyPath() = runBlocking {
        val numTopics = 10
        val messagesToPublish = 25
        val topicPrefix = "multiSubscription"

        val subscriptionResponses = (1..numTopics).map { i ->
            topicClient.subscribe(cacheName, "$topicPrefix$i")
        }.map { response ->
            when (response) {
                is TopicSubscribeResponse.Subscription -> {
                    response
                }

                is TopicSubscribeResponse.Error -> {
                    throw Exception("Got an unexpected subscription response: $response")
                }
            }
        }

        launch {
            delay(2000)

            for (i in 0 until messagesToPublish) {
                val randomTopic = (0 until numTopics).random() + 1
                val messageId = "message$i"
                val topic = "$topicPrefix$randomTopic"
                val publishResponse = topicClient.publish(cacheName, topic, messageId)
                assert(publishResponse is TopicPublishResponse.Success)
                delay(100)
            }
            for (i in 0..numTopics) {
                val topic = "$topicPrefix$i"
                val publishResponse = topicClient.publish(cacheName, topic, "done")
                assert(publishResponse is TopicPublishResponse.Success)
                delay(100)
            }
        }

        val receivedCount = AtomicInteger(0)
        val subscriptionFlows = subscriptionResponses.map { subscription ->
            flow {
                subscription.onEach { emit(it) }
                    .takeWhile { it is TopicMessage.Text && it.value != "done" }
                    .onEach { receivedCount.incrementAndGet() }
                    .collect()
            }
        }

        val mergedFlow = merge(*subscriptionFlows.toTypedArray())
        mergedFlow.collect()

        assert(receivedCount.get() == messagesToPublish) { "Expected $messagesToPublish messages, got $receivedCount" }
    }
}
