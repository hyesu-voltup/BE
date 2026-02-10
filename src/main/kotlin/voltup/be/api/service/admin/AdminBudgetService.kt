package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.SystemDailyBudget
import voltup.be.api.repository.DailyBudgetRepository
import voltup.be.api.repository.SystemDailyBudgetRepository
import java.time.LocalDate

/**
 * 어드민 일일 예산 관리 서비스.
 * 목적: 오늘 예산 잔액 확인 및 강제 설정. 어드민-유저 로직 일치(동일 한도 10만 P).
 */
@Service
class AdminBudgetService(
    private val systemDailyBudgetRepository: SystemDailyBudgetRepository,
    private val dailyBudgetRepository: DailyBudgetRepository
) {

    @Transactional(readOnly = true)
    fun getTodayBudget(): AdminBudgetDto {
        val today = LocalDate.now()
        val participantCount = dailyBudgetRepository.countByBudgetDateAndCancelledFalse(today)
        val budget = systemDailyBudgetRepository.findByBudgetDate(today)
            ?: return AdminBudgetDto(
                budgetDate = today,
                totalGranted = 0L,
                remaining = SystemDailyBudget.DAILY_LIMIT,
                participantCount = participantCount
            )
        return AdminBudgetDto(
            budgetDate = budget.budgetDate,
            totalGranted = budget.totalGranted,
            remaining = (SystemDailyBudget.DAILY_LIMIT - budget.totalGranted).coerceAtLeast(0),
            participantCount = participantCount
        )
    }

    /**
     * 당일 잔여 예산(remaining)을 강제 설정. 내부적으로 totalGranted = DAILY_LIMIT - remaining 으로 반영.
     * 이미 지급된 금액보다 잔여를 적게 설정(즉 totalGranted를 늘리려는 경우)은 불가 → C016.
     */
    @Transactional
    fun patchTodayBudget(remaining: Long): AdminBudgetDto {
        require(remaining >= 0) { "잔여 예산은 0 이상이어야 합니다." }
        require(remaining <= SystemDailyBudget.DAILY_LIMIT) {
            "잔여 예산은 ${SystemDailyBudget.DAILY_LIMIT}을 초과할 수 없습니다."
        }
        val today = LocalDate.now()
        val budget = getOrCreateSystemBudgetWithLock(today)
        val newTotalGranted = SystemDailyBudget.DAILY_LIMIT - remaining
        if (newTotalGranted < budget.totalGranted) {
            throw voltup.be.api.exception.BusinessException(
                voltup.be.api.exception.ErrorCode.BUDGET_CANNOT_DECREASE
            )
        }
        budget.totalGranted = newTotalGranted
        budget.touch()
        val participantCount = dailyBudgetRepository.countByBudgetDateAndCancelledFalse(today)
        return AdminBudgetDto(
            budgetDate = budget.budgetDate,
            totalGranted = budget.totalGranted,
            remaining = (SystemDailyBudget.DAILY_LIMIT - budget.totalGranted).coerceAtLeast(0),
            participantCount = participantCount
        )
    }

    private fun getOrCreateSystemBudgetWithLock(today: LocalDate): SystemDailyBudget {
        systemDailyBudgetRepository.findByBudgetDateForUpdate(today)?.let { return it }
        try {
            systemDailyBudgetRepository.saveAndFlush(SystemDailyBudget(budgetDate = today, totalGranted = 0L))
        } catch (_: org.springframework.dao.DataIntegrityViolationException) { }
        return systemDailyBudgetRepository.findByBudgetDateForUpdate(today)!!
    }

    data class AdminBudgetDto(
        val budgetDate: LocalDate,
        val totalGranted: Long,
        val remaining: Long,
        val participantCount: Long
    )
}
