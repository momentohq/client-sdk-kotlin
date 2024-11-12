package software.momento.kotlin.sdk.exceptions

import software.momento.kotlin.sdk.internal.MomentoTransportErrorDetails

private enum class LimitExceededMessageWrapper(val message: String) {
    TOPIC_SUBSCRIPTIONS_LIMIT_EXCEEDED("Topic subscriptions limit exceeded for this account"),
    OPERATIONS_RATE_LIMIT_EXCEEDED("Request rate limit exceeded for this account"),
    THROUGHPUT_RATE_LIMIT_EXCEEDED("Bandwidth limit exceeded for this account"),
    REQUEST_SIZE_LIMIT_EXCEEDED("Request size limit exceeded for this account"),
    ITEM_SIZE_LIMIT_EXCEEDED("Item size limit exceeded for this account"),
    ELEMENT_SIZE_LIMIT_EXCEEDED("Element size limit exceeded for this account"),
    UNKNOWN_LIMIT_EXCEEDED("Limit exceeded for this account")
}

public class LimitExceededException(
    errCause: String? = null,
    transportErrorDetails: MomentoTransportErrorDetails,
    cause: Throwable? = null
) : MomentoServiceException(
    MomentoErrorCode.LIMIT_EXCEEDED_ERROR,
    determineMessageWrapper(errCause, transportErrorDetails),
    cause,
    transportErrorDetails
) {
    private companion object {
        private fun determineMessageWrapper(
            errCause: String?,
            transportErrorDetails: MomentoTransportErrorDetails
        ): String {
            // First, determine the message based on `errCause`
            errCause?.let {
                val lowerCaseErrCause = it.lowercase()

                when (lowerCaseErrCause) {
                    "topic_subscriptions_limit_exceeded" ->
                        return LimitExceededMessageWrapper.TOPIC_SUBSCRIPTIONS_LIMIT_EXCEEDED.message
                    "operations_rate_limit_exceeded" ->
                        return LimitExceededMessageWrapper.OPERATIONS_RATE_LIMIT_EXCEEDED.message
                    "throughput_rate_limit_exceeded" ->
                        return LimitExceededMessageWrapper.THROUGHPUT_RATE_LIMIT_EXCEEDED.message
                    "request_size_limit_exceeded" ->
                        return LimitExceededMessageWrapper.REQUEST_SIZE_LIMIT_EXCEEDED.message
                    "item_size_limit_exceeded" ->
                        return LimitExceededMessageWrapper.ITEM_SIZE_LIMIT_EXCEEDED.message
                    "element_size_limit_exceeded" ->
                        return LimitExceededMessageWrapper.ELEMENT_SIZE_LIMIT_EXCEEDED.message
                    else -> { /* Do nothing */ }
                }
            }

            // If `errCause` is null, fall back to checking transport error details
            transportErrorDetails.grpc.details.lowercase().let { details ->
                when {
                    "subscribers" in details -> return LimitExceededMessageWrapper.TOPIC_SUBSCRIPTIONS_LIMIT_EXCEEDED.message
                    "operations" in details -> return LimitExceededMessageWrapper.OPERATIONS_RATE_LIMIT_EXCEEDED.message
                    "throughput" in details -> return LimitExceededMessageWrapper.THROUGHPUT_RATE_LIMIT_EXCEEDED.message
                    "request limit" in details -> return LimitExceededMessageWrapper.REQUEST_SIZE_LIMIT_EXCEEDED.message
                    "item size" in details -> return LimitExceededMessageWrapper.ITEM_SIZE_LIMIT_EXCEEDED.message
                    "element size" in details -> return LimitExceededMessageWrapper.ELEMENT_SIZE_LIMIT_EXCEEDED.message
                    else -> { /* Do nothing */}
                }
            }

            // Default message if no conditions match
            return LimitExceededMessageWrapper.UNKNOWN_LIMIT_EXCEEDED.message
        }
    }
}
