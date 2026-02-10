package voltup.be.api.service.v1

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.SystemDailyBudget
import voltup.be.api.dto.v1.RouletteStatusResponse
import voltup.be.api.repository.DailyBudgetRepository
import voltup.be.api.repository.SystemDailyBudgetRepository
import java.time.LocalDate

/**
 * 룰렛 상태 조회 서비스 (일반 사용자).
 * 목적: 당일 일일 예산 잔여량·참여 여부를 제공하여 프론트에서 "포인트 다 썼는지, 남은 포인트 얼마인지" 표시.
 */
@Service
class RouletteStatusService(
    private val systemDailyBudgetRepository: SystemDailyBudgetRepository,
    private val dailyBudgetRepository: DailyBudgetRepository
) {

    /**
     * 당일 룰렛 상태 조회 (잔여 예산, 당일 참여 여부).
     * @param userId 사용자 ID (참여 여부 판단용)
     * @return remainingBudget 오늘 남은 일일 예산(P), alreadyParticipated 당일 참여 여부
     */
    @Transactional(readOnly = true)
    fun getStatus(userId: Long): RouletteStatusResponse {
        val today = LocalDate.now()
        val remainingBudget = systemDailyBudgetRepository.findByBudgetDate(today)
            ?.let { (SystemDailyBudget.DAILY_LIMIT - it.totalGranted).coerceAtLeast(0L) }
            ?: SystemDailyBudget.DAILY_LIMIT
        val alreadyParticipated = dailyBudgetRepository.existsByUserIdAndBudgetDate(userId, today)
        return RouletteStatusResponse(
            remainingBudget = remainingBudget,
            alreadyParticipated = alreadyParticipated
        )
    }
}
