package voltup.be.api.exception

/**
 * 룰렛 취소 시 회수할 포인트가 유저 잔액보다 많을 때.
 * 목적: 회수 불가 시 명시적 예외.
 */
class InsufficientPointForReclaimException(
    val currentBalance: Long,
    val reclaimAmount: Long,
    message: String? = "룰렛 취소 회수 불가: 잔액=$currentBalance, 회수 요청=$reclaimAmount"
) : BusinessException(ErrorCode.INSUFFICIENT_POINT_FOR_RECLAIM, message)
