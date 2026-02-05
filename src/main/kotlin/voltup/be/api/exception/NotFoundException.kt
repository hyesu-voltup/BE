package voltup.be.api.exception

/**
 * 리소스 미존재 시 사용 (User, Product, Point 등).
 * 목적: @RestControllerAdvice에서 404 + ErrorCode 반환.
 */
class NotFoundException(
    errorCode: ErrorCode,
    message: String? = errorCode.message
) : BusinessException(errorCode, message)
