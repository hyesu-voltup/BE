package voltup.be.api.exception

/**
 * 일일 예산 초과 시 (당첨금 지급 불가).
 * 목적: (오늘 총 지급 + 이번 당첨금) > 100,000 시 꽝/예산 소진 반환.
 */
class InsufficientBudgetException(
    val currentGranted: Long,
    val requestAmount: Long,
    val dailyLimit: Long,
    message: String? = "일일 예산 초과: 현재 지급=$currentGranted, 요청=$requestAmount, 한도=$dailyLimit"
) : BusinessException(ErrorCode.INSUFFICIENT_BUDGET, message)
