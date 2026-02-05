package voltup.be.api.exception

/**
 * 비즈니스 규칙 위반 시 사용하는 커스텀 예외.
 * 목적: ErrorCode와 함께 @RestControllerAdvice에서 일괄 응답 변환.
 */
open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.message,
    cause: Throwable? = null
) : RuntimeException(message, cause)
