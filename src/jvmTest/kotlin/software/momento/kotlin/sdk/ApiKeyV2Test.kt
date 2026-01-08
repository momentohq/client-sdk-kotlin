package software.momento.kotlin.sdk

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
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
import software.momento.kotlin.sdk.config.Configurations
import software.momento.kotlin.sdk.config.TopicConfigurations
import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import software.momento.kotlin.sdk.exceptions.NotFoundException
import software.momento.kotlin.sdk.requests.CollectionTtl
import software.momento.kotlin.sdk.responses.cache.DeleteResponse
import software.momento.kotlin.sdk.responses.cache.GetResponse
import software.momento.kotlin.sdk.responses.cache.SetResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheCreateResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheDeleteResponse
import software.momento.kotlin.sdk.responses.cache.control.CacheListResponse
import software.momento.kotlin.sdk.responses.cache.list.ListConcatenateBackResponse
import software.momento.kotlin.sdk.responses.cache.list.ListConcatenateFrontResponse
import software.momento.kotlin.sdk.responses.cache.list.ListFetchResponse
import software.momento.kotlin.sdk.responses.cache.list.ListLengthResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPopBackResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPopFrontResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPushBackResponse
import software.momento.kotlin.sdk.responses.cache.list.ListPushFrontResponse
import software.momento.kotlin.sdk.responses.cache.list.ListRemoveValueResponse
import software.momento.kotlin.sdk.responses.cache.list.ListRetainResponse
import software.momento.kotlin.sdk.responses.topic.TopicMessage
import software.momento.kotlin.sdk.responses.topic.TopicPublishResponse
import software.momento.kotlin.sdk.responses.topic.TopicSubscribeResponse

open class ApiKeyV2Test {
    companion object {
        val cacheName: String = System.getenv("TEST_CACHE_NAME") ?: "kotlin-jvm-integration-v2-${UUID.randomUUID()}"

        lateinit var cacheClient: CacheClient
        lateinit var topicClient: TopicClient

        @JvmStatic
        @BeforeClass
        fun createClients() {
            cacheClient = CacheClient(
                credentialProvider = CredentialProvider.fromEnvVarV2(),
                configuration = Configurations.Laptop.latest,
                itemDefaultTtl = 60.seconds
            )

            topicClient = TopicClient(
                credentialProvider = CredentialProvider.fromEnvVarV2(),
                configuration = TopicConfigurations.Laptop.latest
            )

            runBlocking {
                val cacheClientResponse = cacheClient.createCache(cacheName)
                if (cacheClientResponse is CacheCreateResponse.Error) {
                    throw RuntimeException("Could not create cache for tests: " + cacheClientResponse.message)
                }
            }
        }

        @JvmStatic
        @AfterClass
        fun destroyClients() {
            runBlocking { cacheClient.deleteCache(cacheName) }
            cacheClient.close()
            topicClient.close()
        }
    }

    // Control plane

    @Test
    fun createDeleteCacheHappyPath() = runTest {
        val cacheName = "kotlin-jvm-create-delete-v2-${UUID.randomUUID()}"

        try {
            var createResponse = cacheClient.createCache(cacheName)
            assert(createResponse is CacheCreateResponse.Success)

            createResponse = cacheClient.createCache(cacheName)
            assert(createResponse is CacheCreateResponse.AlreadyExists)
        } finally {
            var deleteResponse = cacheClient.deleteCache(cacheName)
            assert(deleteResponse is CacheDeleteResponse.Success)

            deleteResponse = cacheClient.deleteCache(cacheName)
            assert((deleteResponse as CacheDeleteResponse.Error).cause is NotFoundException)
        }
    }

    @Test
    fun listCacheHappyPath() = runTest  {
        val cacheName = "kotlin-jvm-create-delete-v2-${UUID.randomUUID()}"
        var createResponse = cacheClient.createCache(cacheName)
        assert(createResponse is CacheCreateResponse.Success)

        try {
            var listCachesResponse = cacheClient.listCaches()
            assert(listCachesResponse is CacheListResponse.Success)

            val caches = (listCachesResponse as CacheListResponse.Success).caches
            val cacheNames = caches.map { it.name }
            assertTrue(cacheName in cacheNames, "$cacheName should be one of the cache names")

        } finally {
            var deleteResponse = cacheClient.deleteCache(cacheName)
            assert(deleteResponse is CacheDeleteResponse.Success)
        }
    }

    // Scalar methods

    @Test
    fun getSetDeleteHappyPath_String() = runTest {
        val key = "cache-key"
        val value = "cache-value"

        var getResponse = cacheClient.get(cacheName, key)
        assertTrue(getResponse is GetResponse.Miss, "Expected Miss, response is $getResponse")

        val setResponse = cacheClient.set(cacheName, key, value)
        assert(setResponse is SetResponse.Success)

        getResponse = cacheClient.get(cacheName, key)
        assert((getResponse as GetResponse.Hit).value == value)

        val deleteResponse = cacheClient.delete(cacheName, key)
        assert(deleteResponse is DeleteResponse.Success)

        getResponse = cacheClient.get(cacheName, key)
        assert(getResponse is GetResponse.Miss)
    }

    // List

    @Test
    fun listConcatenateBackFetchStringHappyPath() = runTest {
        val oldValues = listOf("val1", "val2", "val3")
        val newValues = listOf("val4", "val5", "val6")
        val listName = ""

        var fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var concatResponse =
            cacheClient.listConcatenateBack(cacheName, listName, oldValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)

        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(3, fetchResponse.valueListString.size)
        assertEquals(oldValues, fetchResponse.valueListString)

        concatResponse = cacheClient.listConcatenateBack(cacheName, listName, newValues)
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)

        val expectedList = oldValues + newValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListString.size)
        assertEquals(expectedList, fetchResponse.valueListString)

        // Add the original values again and truncate the list to 6 items
        concatResponse = cacheClient.listConcatenateBack(cacheName, listName, oldValues, 6)
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)
        assertEquals(6, concatResponse.listLength)

        val newExpectedList = newValues + oldValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)

        assertTrue(fetchResponse is ListFetchResponse.Hit)

        assertEquals(6, fetchResponse.valueListString.size)
        assertEquals(newExpectedList.takeLast(6), fetchResponse.valueListString)
    }

    @Test
    fun listConcatenateFrontFetchByteArrayHappyPath() = runTest {
        val oldValues = listOf("val1", "val2", "val3").map { it.toByteArray() }
        val newValues = listOf("val4", "val5", "val6").map { it.toByteArray() }
        val listName = "listConcatenateFrontByteArray"

        var fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var concatResponse = cacheClient.listConcatenateFrontByteArray(
            cacheName,
            listName,
            oldValues,
            null,
            CollectionTtl.of(5.seconds)
        )
        assertTrue(concatResponse is ListConcatenateFrontResponse.Success)

        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(3, fetchResponse.valueListByteArray.size)
        oldValues.zip(fetchResponse.valueListByteArray).forEach { (expected, actual) ->
            assertContentEquals(expected, actual)
        }

        concatResponse = cacheClient.listConcatenateFrontByteArray(cacheName, listName, newValues)
        assertTrue(concatResponse is ListConcatenateFrontResponse.Success)

        val expectedList = newValues + oldValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListByteArray.size)
        expectedList.zip(fetchResponse.valueListByteArray).forEach { (expected, actual) ->
            assertContentEquals(expected, actual)
        }

        // Add the original values again and truncate the list to 6 items
        concatResponse =
            cacheClient.listConcatenateFrontByteArray(cacheName, listName, oldValues, 6)
        assertTrue(concatResponse is ListConcatenateFrontResponse.Success)

        val newExpectedList = oldValues + newValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListByteArray.size)
        newExpectedList.zip(fetchResponse.valueListByteArray).forEach { (expected, actual) ->
            assertContentEquals(expected, actual)
        }
    }

     @Test
    fun listLengthHappyPath() = runTest {
        val values = listOf("val1", "val2", "val3")
        val listName = "listLength"

        var lengthResponse = cacheClient.listLength(cacheName, listName)
        assertTrue(
            lengthResponse is ListLengthResponse.Miss,
            "Expected Miss, response is $lengthResponse"
        )

        val concatResponse =
            cacheClient.listConcatenateBack(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)


        lengthResponse = cacheClient.listLength(cacheName, listName)
        assertTrue(
            lengthResponse is ListLengthResponse.Hit,
            "Expected Miss, response is $lengthResponse"
        )
        assertEquals(3, lengthResponse.listLength)
    }

    @Test
    fun listPushBackStringHappyPath() = runTest {
        val oldValue = "val1"
        val newValue = "val2"
        val listName = "listPushBackString"

        var fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var pushBackResponse =
            cacheClient.listPushBack(cacheName, listName, oldValue, null, CollectionTtl.of(5.seconds))
        assertTrue(pushBackResponse is ListPushBackResponse.Success)

        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(1, fetchResponse.valueListString.size)
        assertTrue(fetchResponse.valueListString.contains(oldValue))

        pushBackResponse = cacheClient.listPushBack(cacheName, listName, oldValue)
        assertTrue(pushBackResponse is ListPushBackResponse.Success)

        val expectedList = listOf(oldValue, oldValue)
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListString.size)
        assertEquals(expectedList, fetchResponse.valueListString)

        pushBackResponse = cacheClient.listPushBack(cacheName, listName, newValue, 2)
        assertTrue(pushBackResponse is ListPushBackResponse.Success)

        val newExpectedList = listOf(oldValue, newValue)
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListString.size)
        assertEquals(newExpectedList, fetchResponse.valueListString)
    }

    @Test
    fun listPushFrontByteArrayHappyPath() = runTest {
        val oldValue = "val1".toByteArray()
        val newValue = "val2".toByteArray()
        val listName = "listByteArray"

        var fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var pushFrontResponse =
            cacheClient.listPushFront(cacheName, listName, oldValue, null, CollectionTtl.of(5.seconds))
        assertTrue(pushFrontResponse is ListPushFrontResponse.Success)
        assertEquals(1, pushFrontResponse.listLength)

        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(1, fetchResponse.valueListByteArray.size)
        assertTrue(fetchResponse.valueListByteArray.any { it.contentEquals(oldValue) })

        pushFrontResponse = cacheClient.listPushFront(cacheName, listName, oldValue)
        assertTrue(pushFrontResponse is ListPushFrontResponse.Success)
        assertEquals(2, pushFrontResponse.listLength)

        val expectedList = listOf(oldValue, oldValue)
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListByteArray.size)
        expectedList.forEachIndexed { index, byteArray ->
            assertContentEquals(
                byteArray,
                (fetchResponse as ListFetchResponse.Hit).valueListByteArray[index]
            )
        }

        pushFrontResponse = cacheClient.listPushFront(cacheName, listName, newValue, 2)
        assertTrue(pushFrontResponse is ListPushFrontResponse.Success)
        assertEquals(2, pushFrontResponse.listLength)

        val newExpectedList = listOf(newValue, oldValue)
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListByteArray.size)
        newExpectedList.forEachIndexed { index, byteArray ->
            assertContentEquals(byteArray, fetchResponse.valueListByteArray[index])
        }
    }

    @Test
    fun listPopFrontHappyPath() = runTest {
        val values = listOf("val1", "val2", "val3")
        val listName = "listPopFront"

        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        val concatenateResponse =
            cacheClient.listConcatenateBack(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateBackResponse.Success)

        // Pop the value from the front of the list
        var popResponse = cacheClient.listPopFront(cacheName, listName)
        assertTrue(popResponse is ListPopFrontResponse.Hit)
        assertEquals("val1", popResponse.valueString)

        // Pop the next value from the front of the list
        popResponse = cacheClient.listPopFront(cacheName, listName)
        assertTrue(popResponse is ListPopFrontResponse.Hit)
        assertContentEquals("val2".toByteArray(), popResponse.valueByteArray)
    }

    @Test
    fun listPopBackHappyPath() = runTest {
        val values = listOf("val1", "val2", "val3")
        val listName = "listPopBack"

        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        val concatenateResponse =
            cacheClient.listConcatenateBack(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateBackResponse.Success)

        // Pop the value from the back of the list
        var popResponse = cacheClient.listPopBack(cacheName, listName)
        assertTrue(popResponse is ListPopBackResponse.Hit)
        assertEquals("val3", popResponse.valueString)

        // Pop the next value from the back of the list
        popResponse = cacheClient.listPopBack(cacheName, listName)
        assertTrue(popResponse is ListPopBackResponse.Hit)
        assertContentEquals("val2".toByteArray(), popResponse.valueByteArray)
    }

    @Test
    fun listRemoveValueStringHappyPath() = runTest {
        val values = listOf("val1", "val1", "val2", "val3", "val4")
        val listName = "listStringRemove"

        val concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        // Remove value from list
        val removeValue = "val1"
        val removeResponse = cacheClient.listRemoveValue(cacheName, listName, removeValue)
        assertTrue(removeResponse is ListRemoveValueResponse.Success)

        // Fetch list and verify the values
        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        val expectedList = listOf("val2", "val3", "val4")
        assertEquals(3, fetchResponse.valueListString.size)
        assertTrue(expectedList.containsAll(fetchResponse.valueListString))
    }

    @Test
    fun listRetainAllValuesWhenListRetainWithPositiveStartEndIndices() = runTest {
        val listName = "listRetainWithPositiveStartEndIndices"
        val stringValues = listOf("val1", "val2", "val3", "val4")

        val concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        // Retain values from index 1 to 3 (exclusive)
        val retainResponse = cacheClient.listRetain(cacheName, listName, 1, 3)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val expectedList = listOf("val2", "val3")
        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListString.size)
        assertTrue(expectedList.containsAll(fetchResponse.valueListString))
    }

    // Topics

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
}