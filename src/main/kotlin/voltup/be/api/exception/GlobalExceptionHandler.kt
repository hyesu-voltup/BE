package voltup.be.api.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리.
 * 목적: Controller에서 발생한 BusinessException 및 기타 예외를 일관된 API 응답 형식으로 변환.
 */
@RestControllerAdvice(annotations = [RestController::class], basePackages = ["voltup.be.api.controller"])
class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 → HTTP 상태 및 body 매핑.
     * @return ErrorCode 기반 ResponseEntity
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = ex.errorCode.code,
            message = ex.message ?: ex.errorCode.message
        )
        return ResponseEntity.status(ex.errorCode.status).body(body)
    }

    /**
     * DB 유니크 위반 (예: 1인 1회 참여 중복) → 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = ErrorCode.ALREADY_PARTICIPATED.code,
            message = ErrorCode.ALREADY_PARTICIPATED.message
        )
        return ResponseEntity.status(ErrorCode.ALREADY_PARTICIPATED.status).body(body)
    }

    /**
     * IllegalArgumentException 등 → 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = ErrorCode.BAD_REQUEST.code,
            message = ex.message ?: ErrorCode.BAD_REQUEST.message
        )
        return ResponseEntity.badRequest().body(body)
    }

    /**
     * IllegalStateException (예: 재고 부족) → 400.
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = ErrorCode.BAD_REQUEST.code,
            message = ex.message ?: ErrorCode.BAD_REQUEST.message
        )
        return ResponseEntity.badRequest().body(body)
    }

    /**
     * NotFoundException → 404 (리소스 없음).
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = ex.errorCode.code,
            message = ex.message ?: ex.errorCode.message
        )
        return ResponseEntity.status(ex.errorCode.status).body(body)
    }

    /**
     * 기타 예외 → 500.
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = ErrorCode.INTERNAL_SERVER_ERROR.code,
            message = ErrorCode.INTERNAL_SERVER_ERROR.message
        )
        return ResponseEntity.internalServerError().body(body)
    }
}

/**
 * API 에러 응답 body.
 */
data class ErrorResponse(
    val code: String,
    val message: String
)
