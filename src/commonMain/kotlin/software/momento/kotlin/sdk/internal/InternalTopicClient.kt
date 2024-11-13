package software.momento.kotlin.sdk.internal

import software.momento.kotlin.sdk.auth.CredentialProvider
import software.momento.kotlin.sdk.config.TopicConfiguration
import software.momento.kotlin.sdk.responses.topic.TopicPublishResponse
import software.momento.kotlin.sdk.responses.topic.TopicSubscribeResponse
import java.io.Closeable

internal expect class InternalTopicClient(credentialProvider: CredentialProvider, configuration: TopicConfiguration) :
    InternalClient {

    internal suspend fun publish(cacheName: String, topicName: String, value: String): TopicPublishResponse

    internal suspend fun publish(cacheName: String, topicName: String, value: ByteArray): TopicPublishResponse

    internal suspend fun subscribe(cacheName: String, topicName: String, resumeAtTopicSequenceNumber: Long? = null, resumeAtTopicSequencePage: Long? = null): TopicSubscribeResponse
}
