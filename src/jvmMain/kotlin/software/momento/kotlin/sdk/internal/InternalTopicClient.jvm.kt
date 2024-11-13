package software.momento.kotlin.sdk.internal

import com.google.protobuf.kotlin.toByteString
import grpc.cache_client.pubsub._PublishRequest
import grpc.cache_client.pubsub._SubscriptionItem
import grpc.cache_client.pubsub._SubscriptionRequest
import grpc.cache_client.pubsub._TopicValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.TopicConfiguration
import software.momento.kotlin.sdk.exceptions.CacheServiceExceptionMapper
import software.momento.kotlin.sdk.exceptions.InternalServerException
import software.momento.kotlin.sdk.exceptions.MomentoErrorCode
import software.momento.kotlin.sdk.exceptions.SdkException
import software.momento.kotlin.sdk.internal.utils.ValidationUtils
import software.momento.kotlin.sdk.responses.topic.TopicMessage
import software.momento.kotlin.sdk.responses.topic.TopicPublishResponse
import software.momento.kotlin.sdk.responses.topic.TopicSubscribeResponse

internal actual class InternalTopicClient actual constructor(
    credentialProvider: CredentialProvider, configuration: TopicConfiguration
) : InternalClient() {
    private val stubsManager: TopicGrpcStubsManager

    init {
        stubsManager = TopicGrpcStubsManager(credentialProvider)
    }

    internal actual suspend fun publish(
        cacheName: String, topicName: String, value: String
    ): TopicPublishResponse {
        val topicValue = _TopicValue.newBuilder().setText(value).build()
        return validateAndSendPublish(cacheName, topicName, topicValue)
    }

    internal actual suspend fun publish(
        cacheName: String, topicName: String, value: ByteArray
    ): TopicPublishResponse {
        val topicValue = _TopicValue.newBuilder().setBinary(value.toByteString()).build()
        return validateAndSendPublish(cacheName, topicName, topicValue)
    }

    private suspend fun validateAndSendPublish(
        cacheName: String, topicName: String, topicValue: _TopicValue
    ): TopicPublishResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
        }.fold(onSuccess = {
            sendPublish(cacheName, topicName, topicValue)
        }, onFailure = { e ->
            if (e is CancellationException) {
                throw e // Propagate cancellation
            }
            TopicPublishResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    private suspend fun sendPublish(
        cacheName: String, topicName: String, topicValue: _TopicValue
    ): TopicPublishResponse {
        val metadata = metadataWithCache(cacheName)

        val request =
            _PublishRequest.newBuilder().setCacheName(cacheName).setTopic(topicName).setValue(topicValue).build()

        return runCatching {
            stubsManager.unaryStub.publish(request, metadata)
        }.fold(onSuccess = {
            TopicPublishResponse.Success
        }, onFailure = { e ->
            if (e is CancellationException) {
                throw e // Propagate cancellation
            }
            TopicPublishResponse.Error(CacheServiceExceptionMapper.convert(e, metadata))
        })
    }

    internal actual suspend fun subscribe(
        cacheName: String, topicName: String, resumeAtTopicSequenceNumber: Long?, resumeAtTopicSequencePage: Long?
    ): TopicSubscribeResponse {
        return runCatching {
            ValidationUtils.requireValidCacheName(cacheName)
            sendSubscribe(cacheName, topicName, resumeAtTopicSequenceNumber, resumeAtTopicSequencePage)
        }.fold(onSuccess = { flow ->
            TopicSubscribeResponse.Subscription(flow)
        }, onFailure = { e ->
            if (e is CancellationException) {
                throw e // Propagate cancellation
            }
            TopicSubscribeResponse.Error(CacheServiceExceptionMapper.convert(e))
        })
    }

    /**
     * As long as the subscription hasn't been cancelled and an unrecoverable error hasn't occurred,
     * this method will create a topic subscription flow and emit binary and text messages. It tracks
     * the first message so it can throw an error, otherwise errors are converted into messages and emitted.
     */

    private suspend fun sendSubscribe(
        cacheName: String, topicName: String, resumeAtTopicSequenceNumber: Long?, resumeAtTopicSequencePage: Long?
    ): Flow<TopicMessage> = flow {
        var lastSequenceNumber: Long? = resumeAtTopicSequenceNumber ?: 0
        var lastSequencePage: Long? = resumeAtTopicSequencePage ?: 0
        var isFirstMessage = true
        var retry = true

        val metadata = metadataWithCache(cacheName)
        while (retry && currentCoroutineContext().isActive) {
            try {
                val request = buildSubscriptionRequest(cacheName, topicName, lastSequenceNumber, lastSequencePage)
                val subscriptionFlow = stubsManager.streamingStub.subscribe(request, metadata)
                subscriptionFlow.onEach { message ->
                    if (isFirstMessage) {
                        isFirstMessage = false
                        if (message.kindCase != _SubscriptionItem.KindCase.HEARTBEAT) {
                            throw InternalServerException(
                                "Expected heartbeat message for topic $topicName on cache $cacheName. Got: ${message.kindCase}"
                            )
                        }
                    }
                }.mapNotNull { message ->
                    convertSubscriptionItem(message)
                }.collect { (topicMessage, sequenceNumber, sequencePage) ->
                    if (!isFirstMessage) {
                        topicMessage?.let { emit(it) }
                        sequenceNumber?.let { lastSequenceNumber = it }
                        sequencePage?.let { lastSequencePage = it }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e // Propagate cancellation
                }
                val sdkException = CacheServiceExceptionMapper.convert(e, metadata)
                if (isFirstMessage) {
                    throw sdkException
                } else {
                    if (isRecoverableError(sdkException)) {
                        delay(5000)
                    } else {
                        emit(TopicMessage.Error(sdkException))
                        retry = false
                    }
                }
            }
        }
    }

    private fun buildSubscriptionRequest(
        cacheName: String, topicName: String, lastSequenceNumber: Long?, lastSequencePage: Long?
    ): _SubscriptionRequest {
        return _SubscriptionRequest.newBuilder().apply {
            this.cacheName = cacheName
            this.topic = topicName
            if (lastSequenceNumber != null) {
                this.resumeAtTopicSequenceNumber = lastSequenceNumber
            }
            if (lastSequencePage != null) {
                this.sequencePage = lastSequencePage
            }
        }.build()
    }

    private data class ConvertedMessage(val message: TopicMessage?, val lastSequenceNumber: Long?, val lastSequencePage: Long?)

    private fun convertSubscriptionItem(subscriptionItem: _SubscriptionItem): ConvertedMessage? {
        val lastSequenceNumber: Long? = when (subscriptionItem.kindCase) {
            _SubscriptionItem.KindCase.ITEM -> subscriptionItem.item.topicSequenceNumber
            _SubscriptionItem.KindCase.DISCONTINUITY -> subscriptionItem.discontinuity.newTopicSequence
            else -> null
        }

        val lastSequencePage: Long? = when (subscriptionItem.kindCase) {
            _SubscriptionItem.KindCase.ITEM -> subscriptionItem.item.sequencePage
            _SubscriptionItem.KindCase.DISCONTINUITY -> subscriptionItem.discontinuity.newSequencePage
            else -> null
        }

        val topicMessage: TopicMessage? = when (subscriptionItem.kindCase) {
            _SubscriptionItem.KindCase.ITEM -> {
                val tokenId = subscriptionItem.item.publisherId.takeIf { it.isNotEmpty() }
                when (subscriptionItem.item.value.kindCase) {
                    _TopicValue.KindCase.TEXT -> TopicMessage.Text(subscriptionItem.item.value.text, tokenId)
                    _TopicValue.KindCase.BINARY -> TopicMessage.Binary(
                        subscriptionItem.item.value.binary.toByteArray(), tokenId
                    )

                    else -> null
                }
            }

            else -> null
        }

        return if (topicMessage != null || lastSequenceNumber != null || lastSequencePage != null) {
            ConvertedMessage(topicMessage, lastSequenceNumber, lastSequencePage)
        } else {
            null
        }
    }

    private fun isRecoverableError(e: SdkException): Boolean {
        return when (e.errorCode) {
            MomentoErrorCode.PERMISSION_ERROR, MomentoErrorCode.AUTHENTICATION_ERROR -> false

            else -> true
        }
    }

    override fun close() {
        stubsManager.close()
    }
}
