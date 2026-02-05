package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.SystemDailyBudget
import voltup.be.api.repository.SystemDailyBudgetRepository
import java.time.LocalDate

/**
 * 어드민 일일 예산 관리 서비스.
 * 목적: 오늘 예산 잔액 확인 및 강제 설정. 어드민-유저 로직 일치(동일 한도 10만 P).
 */
@Service
class AdminBudgetService(
    private val systemDailyBudgetRepository: SystemDailyBudgetRepository
) {

    @Transactional(readOnly = true)
    fun getTodayBudget(): AdminBudgetDto {
        val today = LocalDate.now()
        val budget = systemDailyBudgetRepository.findByBudgetDate(today)
            ?: return AdminBudgetDto(
                budgetDate = today,
                totalGranted = 0L,
                remaining = SystemDailyBudget.DAILY_LIMIT
            )
        return AdminBudgetDto(
            budgetDate = budget.budgetDate,
            totalGranted = budget.totalGranted,
            remaining = (SystemDailyBudget.DAILY_LIMIT - budget.totalGranted).coerceAtLeast(0)
        )
    }

    @Transactional
    fun patchTodayBudget(forceTotalGranted: Long): AdminBudgetDto {
        require(forceTotalGranted >= 0) { "총 지급액은 0 이상이어야 합니다." }
        require(forceTotalGranted <= SystemDailyBudget.DAILY_LIMIT) {
            "총 지급액은 ${SystemDailyBudget.DAILY_LIMIT}을 초과할 수 없습니다."
        }
        val today = LocalDate.now()
        val budget = getOrCreateSystemBudgetWithLock(today)
        budget.totalGranted = forceTotalGranted
        budget.touch()
        return AdminBudgetDto(
            budgetDate = budget.budgetDate,
            totalGranted = budget.totalGranted,
            remaining = (SystemDailyBudget.DAILY_LIMIT - budget.totalGranted).coerceAtLeast(0)
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
        val remaining: Long
    )
}
