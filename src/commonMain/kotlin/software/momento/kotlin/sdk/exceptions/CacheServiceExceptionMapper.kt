package software.momento.kotlin.sdk.exceptions

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import software.momento.kotlin.sdk.internal.MomentoGrpcErrorDetails
import software.momento.kotlin.sdk.internal.MomentoTransportErrorDetails

/**
 * Mapper from any exception that may occur during a cache client call to the appropriate [ ].
 */
public object CacheServiceExceptionMapper {
    private const val SDK_FAILED_TO_PROCESS_THE_REQUEST = "SDK Failed to process the request."

    /**
     * Common Handler for converting exceptions encountered by the SDK. Any specialized exception
     * handling should be performed before calling this.
     *
     * @param e The exception to convert.
     * @param metadata Metadata from the grpc request that caused the error.
     * @return The converted exception.
     */
    public fun convert(e: Throwable, metadata: Metadata? = null): SdkException {
        return when (e) {
            is IllegalArgumentException -> InvalidArgumentException(e.message)
            is SdkException -> e
            is StatusRuntimeException -> {
                val statusCode = e.status.code
                val errorDetails = MomentoTransportErrorDetails(
                    MomentoGrpcErrorDetails(statusCode, e.message!!, metadata)
                )
                when (statusCode) {
                    Status.Code.INVALID_ARGUMENT, Status.Code.UNIMPLEMENTED,
                    Status.Code.OUT_OF_RANGE, Status.Code.FAILED_PRECONDITION -> BadRequestException(
                        e, errorDetails
                    )
                    Status.Code.CANCELLED -> CancellationException(e, errorDetails)
                    Status.Code.DEADLINE_EXCEEDED -> TimeoutException(e, errorDetails)
                    Status.Code.PERMISSION_DENIED -> PermissionDeniedException(
                        e, errorDetails
                    )
                    Status.Code.UNAUTHENTICATED -> AuthenticationException(e, errorDetails)
                    Status.Code.RESOURCE_EXHAUSTED -> LimitExceededException(
                        e, errorDetails
                    )
                    Status.Code.NOT_FOUND -> NotFoundException(e, errorDetails)
                    Status.Code.ALREADY_EXISTS -> AlreadyExistsException(e, errorDetails)
                    Status.Code.UNKNOWN -> UnknownServiceException(e, errorDetails)
                    Status.Code.UNAVAILABLE -> ServerUnavailableException(e, errorDetails)
                    Status.Code.ABORTED, Status.Code.INTERNAL, Status.Code.DATA_LOSS -> InternalServerException(
                        e, errorDetails
                    )
                    else -> InternalServerException(e, errorDetails)
                }
            }
            else -> UnknownException(SDK_FAILED_TO_PROCESS_THE_REQUEST, e)
        }
    }
}
