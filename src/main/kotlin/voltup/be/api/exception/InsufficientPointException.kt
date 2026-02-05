package voltup.be.api.exception

/**
 * 포인트 잔액 부족 시 도메인(Point)에서 발생.
 * 목적: ErrorCode.INSUFFICIENT_POINT와 함께 클라이언트에 잔액/필요량 전달 가능.
 */
class InsufficientPointException(
    val currentBalance: Long,
    val requiredAmount: Long,
    message: String? = "포인트가 부족합니다. (잔액: $currentBalance, 필요: $requiredAmount)"
) : BusinessException(ErrorCode.INSUFFICIENT_POINT, message)
