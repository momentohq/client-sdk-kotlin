package software.momento.kotlin.sdk

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import software.momento.kotlin.sdk.exceptions.InvalidArgumentException
import software.momento.kotlin.sdk.requests.CollectionTtl
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
import kotlin.test.assertContentEquals
import kotlin.time.Duration.Companion.seconds

class CacheClientListTest: BaseJvmTestClass() {

    /** List concatenate */

    @Test
    fun listConcatenateBackFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listConcatenateBack("", "list", listOf())
        assert((response as ListConcatenateBackResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateBackFailsWithInvalidTruncate() = runTest {
        val response = cacheClient.listConcatenateBack("cache", "listName", listOf(), 0)
        assert((response as ListConcatenateBackResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateBackByteArrayFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listConcatenateBackByteArray("", "list", listOf())
        assert((response as ListConcatenateBackResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateBackByteArrayFailsWithInvalidTruncate() = runTest {
        val response = cacheClient.listConcatenateBackByteArray("cache", "listName", listOf(), 0)
        assert((response as ListConcatenateBackResponse.Error).cause is InvalidArgumentException)
    }

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
    fun listConcatenateBackFetchByteArrayHappyPath() = runTest {
        val oldValues = listOf("val1", "val2", "val3").map { it.toByteArray() }
        val newValues = listOf("val4", "val5", "val6").map { it.toByteArray() }
        val listName = "listConcatenateBackByteArray"

        var fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var concatResponse = cacheClient.listConcatenateBackByteArray(
            cacheName,
            listName,
            oldValues,
            null,
            CollectionTtl.of(5.seconds)
        )
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)

        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(3, fetchResponse.valueListByteArray.size)
        oldValues.zip(fetchResponse.valueListByteArray).forEach { (expected, actual) ->
            assertContentEquals(expected, actual)
        }

        concatResponse = cacheClient.listConcatenateBackByteArray(cacheName, listName, newValues)
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)

        val expectedList = oldValues + newValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListByteArray.size)
        expectedList.zip(fetchResponse.valueListByteArray).forEach { (expected, actual) ->
            assertContentEquals(expected, actual)
        }

        // Add the original values again and truncate the list to 6 items
        concatResponse = cacheClient.listConcatenateBackByteArray(cacheName, listName, oldValues, 6)
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)

        val newExpectedList = newValues + oldValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListByteArray.size)
        newExpectedList.zip(fetchResponse.valueListByteArray).forEach { (expected, actual) ->
            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun listConcatenateFrontFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listConcatenateFront("", "list", listOf())
        assert((response as ListConcatenateFrontResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateFrontFailsWithInvalidTruncate() = runTest {
        val response = cacheClient.listConcatenateFront("cache", "listName", listOf(), 0)
        assert((response as ListConcatenateFrontResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateFrontByteArrayFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listConcatenateFront("", "list", listOf())
        assert((response as ListConcatenateFrontResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateFrontByteArrayFailsWithInvalidTruncate() = runTest {
        val response = cacheClient.listConcatenateFront("cache", "listName", listOf(), 0)
        assert((response as ListConcatenateFrontResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listConcatenateFrontStringHappyPath() = runTest {
        val oldValues = listOf("val1", "val2", "val3")
        val newValues = listOf("val4", "val5", "val6")
        val listName = "listConcatenateFrontString"

        var fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var concatResponse =
            cacheClient.listConcatenateFront(cacheName, listName, oldValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatResponse is ListConcatenateFrontResponse.Success)

        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(3, fetchResponse.valueListString.size)
        assertEquals(oldValues, fetchResponse.valueListString)

        concatResponse = cacheClient.listConcatenateFront(cacheName, listName, newValues)
        assertTrue(concatResponse is ListConcatenateFrontResponse.Success)

        val expectedList = newValues + oldValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListString.size)
        assertEquals(expectedList, fetchResponse.valueListString)

        // Concatenate old values again and truncate the list to 6 items
        concatResponse = cacheClient.listConcatenateFront(cacheName, listName, oldValues, 6)
        assertTrue(concatResponse is ListConcatenateFrontResponse.Success)

        // Expected list should be a combination of old and new values, truncated to the last 6 items
        val newExpectedList = oldValues + newValues
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(6, fetchResponse.valueListString.size)
        assertEquals(newExpectedList, fetchResponse.valueListString)
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

    /** Specific list fetch tests */

    @Test
    fun listFetchFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listFetch("", "listName")
        assert((response as ListFetchResponse.Error).cause is InvalidArgumentException)

    }

    @Test
    fun listFetchFailsWithInvalidIndices() = runTest {
        val response = cacheClient.listFetch("cache", "list", 3, 2)
        assert((response as ListFetchResponse.Error).cause is InvalidArgumentException)
    }

    /** List length */
    @Test
    fun listLengthFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listLength("", "listName")
        assert((response as ListLengthResponse.Error).cause is InvalidArgumentException)
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

        var concatResponse =
            cacheClient.listConcatenateBack(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatResponse is ListConcatenateBackResponse.Success)


        lengthResponse = cacheClient.listLength(cacheName, listName)
        assertTrue(
            lengthResponse is ListLengthResponse.Hit,
            "Expected Miss, response is $lengthResponse"
        )
        assertEquals(3, lengthResponse.listLength)
    }

    /** List Push tests */

    @Test
    fun listPushBackFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listPushBack("", "list", "val")
        assert((response as ListPushBackResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listPushBackFailsWithInvalidTruncate() = runTest {
        val response = cacheClient.listPushBack("cache", "listName", "val", 0)
        assert((response as ListPushBackResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listPushFrontFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listPushFront("", "list", "val".toByteArray())
        assert((response as ListPushFrontResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listPushFrontFailsWithInvalidTruncate() = runTest {
        val response = cacheClient.listPushFront("cache", "listName", "val", 0)
        assert((response as ListPushFrontResponse.Error).cause is InvalidArgumentException)
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
    fun listPushBackByteArrayHappyPath() = runTest {
        val oldValue = "val1".toByteArray()
        val newValue = "val2".toByteArray()
        val listName = "listPushBackByteArray"

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
        assertEquals(1, fetchResponse.valueListByteArray.size)
        assertTrue(fetchResponse.valueListByteArray.any { it.contentEquals(oldValue) })

        pushBackResponse = cacheClient.listPushBack(cacheName, listName, oldValue)
        assertTrue(pushBackResponse is ListPushBackResponse.Success)

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

        pushBackResponse = cacheClient.listPushBack(cacheName, listName, newValue, 2)
        assertTrue(pushBackResponse is ListPushBackResponse.Success)

        val newExpectedList = listOf(oldValue, newValue)
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListByteArray.size)
        newExpectedList.forEachIndexed { index, byteArray ->
            assertContentEquals(byteArray, fetchResponse.valueListByteArray[index])
        }
    }

    @Test
    fun listPushFrontStringHappyPath() = runTest {
        val oldValue = "val1"
        val newValue = "val2"
        val listName = "listString"

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
        assertEquals(1, fetchResponse.valueListString.size)
        assertTrue(fetchResponse.valueListString.contains(oldValue))

        pushFrontResponse = cacheClient.listPushFront(cacheName, listName, oldValue)
        assertTrue(pushFrontResponse is ListPushFrontResponse.Success)
        assertEquals(2, pushFrontResponse.listLength)

        val expectedList = listOf(oldValue, oldValue)
        fetchResponse = cacheClient.listFetch(cacheName, listName)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(2, fetchResponse.valueListString.size)
        assertEquals(expectedList, fetchResponse.valueListString)

        pushFrontResponse = cacheClient.listPushFront(cacheName, listName, newValue, 2)
        assertTrue(pushFrontResponse is ListPushFrontResponse.Success)
        assertEquals(2, pushFrontResponse.listLength)

        val newExpectedList = listOf(newValue, oldValue)
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
        fetchResponse as ListFetchResponse.Hit
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

    /** List Pop tests */

    @Test
    fun listPopBackFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listPopBack("", "list")
        assert((response as ListPopBackResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listPopFrontFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listPopFront("", "list")
        assert((response as ListPopFrontResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listPopFrontHappyPath() = runTest {
        val values = listOf("val1", "val2", "val3")
        val listName = "listPopFront"

        var fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var concatenateResponse =
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

        var fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(
            fetchResponse is ListFetchResponse.Miss,
            "Expected Miss, response is $fetchResponse"
        )

        var concatenateResponse =
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

    /** List remove value tests */

    @Test
    fun listRemoveValueFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listRemoveValue("", "list", "value")
        assert((response as ListRemoveValueResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listRemoveValueStringHappyPath() = runTest {
        val values = listOf("val1", "val1", "val2", "val3", "val4")
        val listName = "listStringRemove"

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        // Remove value from list
        val removeValue = "val1"
        val removeResponse = cacheClient.listRemoveValue(cacheName, listName, removeValue)
        assertTrue(removeResponse is ListRemoveValueResponse.Success)

        // Fetch list and verify the values
        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        fetchResponse as ListFetchResponse.Hit
        val expectedList = listOf("val2", "val3", "val4")
        assertEquals(3, fetchResponse.valueListString.size)
        assertTrue(expectedList.containsAll(fetchResponse.valueListString))
    }

    @Test
    fun listRemoveValueByteArrayHappyPath() = runTest {
        val values = listOf("val1", "val1", "val2", "val3", "val4").map { it.toByteArray() }
        val listName = "listByteArrayRemove"

        var concatenateResponse =
            cacheClient.listConcatenateFrontByteArray(cacheName, listName, values, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        // Remove value from list
        val removeValue = "val1".toByteArray()
        val removeResponse = cacheClient.listRemoveValue(cacheName, listName, removeValue)
        assertTrue(removeResponse is ListRemoveValueResponse.Success)

        // Fetch list and verify the values
        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        fetchResponse as ListFetchResponse.Hit
        val expectedList = listOf("val2", "val3", "val4").map { it.toByteArray() }
        assertEquals(3, fetchResponse.valueListByteArray.size)
        expectedList.forEachIndexed { index, byteArray ->
            assertContentEquals(byteArray, fetchResponse.valueListByteArray[index])
        }
    }

    /** List retain tests */
    @Test
    fun listRetainFailsWithInvalidCacheName() = runTest {
        val response = cacheClient.listRetain("", "listName")
        assert((response as ListRetainResponse.Error).cause is InvalidArgumentException)

    }

    @Test
    fun listRetainFailsWithInvalidIndices() = runTest {
        val response = cacheClient.listRetain("cache", "list", 3, 2)
        assert((response as ListRetainResponse.Error).cause is InvalidArgumentException)
    }

    @Test
    fun listRetainAllValuesWhenListRetainWithPositiveStartEndIndices() = runTest {
        val listName = "listRetainWithPositiveStartEndIndices"
        val stringValues = listOf("val1", "val2", "val3", "val4")

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        // Retain values from index 1 to 3 (exclusive)
        val retainResponse = cacheClient.listRetain(cacheName, listName, 1, 3)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val expectedList = listOf("val2", "val3")
        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        fetchResponse as ListFetchResponse.Hit
        assertEquals(2, fetchResponse.valueListString.size)
        assertTrue(expectedList.containsAll(fetchResponse.valueListString))
    }

    @Test
    fun listRetainAllValuesWhenListRetainWithNegativeStartEndIndices() = runTest {
        val listName = "listRetainWithNegativeStartEndIndices"
        val stringValues = listOf("val1", "val2", "val3", "val4")

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        // Retain values from index -3 to -1 (exclusive)
        val retainResponse = cacheClient.listRetain(cacheName, listName, -3, -1)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val expectedList = listOf("val2", "val3")
        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        fetchResponse as ListFetchResponse.Hit
        assertEquals(2, fetchResponse.valueListString.size)
        assertTrue(expectedList.containsAll(fetchResponse.valueListString))
    }

    @Test
    fun shouldRetainAllValuesWhenListRetainWithNullStartIndex() = runTest {
        val listName = "listRetainWithNullStartIndex"
        val stringValues = listOf("val1", "val2", "val3", "val4", "val5", "val6", "val7", "val8")

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        var retainResponse = cacheClient.listRetain(cacheName, listName, null, 7)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val expectedList = stringValues.take(7)
        var fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(expectedList, fetchResponse.valueListString)

        retainResponse = cacheClient.listRetain(cacheName, listName, null, -3)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val newExpectedList = stringValues.take(4)
        fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(newExpectedList, fetchResponse.valueListString)
    }

    @Test
    fun shouldRetainAllValuesWhenListRetainWithNullEndIndex() = runTest {
        val listName = "listRetainWithNullEndIndex"
        val stringValues = listOf("val1", "val2", "val3", "val4", "val5", "val6", "val7", "val8")

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        var retainResponse = cacheClient.listRetain(cacheName, listName, 2, null)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val expectedList = stringValues.drop(2)
        var fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(expectedList, fetchResponse.valueListString)

        retainResponse = cacheClient.listRetain(cacheName, listName, -4, null)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val newExpectedList = stringValues.takeLast(4)
        fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        assertEquals(newExpectedList, fetchResponse.valueListString)
    }

    @Test
    fun shouldRetainAllValuesWhenListRetainWithNullStartAndEndIndices() = runTest {
        val listName = "listRetainWithNullStartAndEndIndices"
        val stringValues = listOf("val1", "val2", "val3", "val4", "val5", "val6", "val7", "val8")

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        val retainResponse = cacheClient.listRetain(cacheName, listName, null, null)
        assertTrue(retainResponse is ListRetainResponse.Success)

        val fetchResponse = cacheClient.listFetch(cacheName, listName, null, null)
        assertTrue(fetchResponse is ListFetchResponse.Hit)
        fetchResponse as ListFetchResponse.Hit
        assertEquals(stringValues, fetchResponse.valueListString)
    }

    @Test
    fun shouldRetainAllValuesWhenListRetainWithInvalidIndices() = runTest {
        val listName = "listName"
        val stringValues = listOf("val1", "val2", "val3", "val4", "val5", "val6", "val7", "val8")

        var concatenateResponse =
            cacheClient.listConcatenateFront(cacheName, listName, stringValues, null, CollectionTtl.of(5.seconds))
        assertTrue(concatenateResponse is ListConcatenateFrontResponse.Success)

        var retainResponse = cacheClient.listRetain(cacheName, listName, 3, 1)
        assertTrue(retainResponse is ListRetainResponse.Error)

        retainResponse = cacheClient.listRetain(cacheName, listName, 3, 3)
        assertTrue(retainResponse is ListRetainResponse.Error)

        retainResponse = cacheClient.listRetain(cacheName, listName, -3, -5)
        assertTrue(retainResponse is ListRetainResponse.Error)
    }
}