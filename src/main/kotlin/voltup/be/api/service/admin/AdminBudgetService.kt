package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.SystemDailyBudget
import voltup.be.api.repository.DailyBudgetRepository
import voltup.be.api.repository.SystemDailyBudgetRepository
import java.time.LocalDate

/**
 * 어드민 일일 예산 관리 서비스.
 * 목적: 오늘 예산 조회·수정. totalGranted(참여자 지급 합)는 수정 불가, 수정 시 dailyLimit(오늘 발급 가능 전체 포인트)만 변경.
 */
@Service
class AdminBudgetService(
    private val systemDailyBudgetRepository: SystemDailyBudgetRepository,
    private val dailyBudgetRepository: DailyBudgetRepository
) {

    /**
     * 오늘 예산 조회.
     * @return totalGranted 참여자들이 룰렛을 통해 받은 포인트 전체 합, remaining 전체 예산에서 그만큼 제외한 남은 예산.
     */
    @Transactional(readOnly = true)
    fun getTodayBudget(): AdminBudgetDto {
        val today = LocalDate.now()
        val participantCount = dailyBudgetRepository.countByBudgetDateAndCancelledFalse(today)
        val budget = systemDailyBudgetRepository.findByBudgetDate(today)
            ?: return AdminBudgetDto(
                budgetDate = today,
                totalGranted = 0L,
                totalLimit = SystemDailyBudget.DEFAULT_DAILY_LIMIT,
                remaining = SystemDailyBudget.DEFAULT_DAILY_LIMIT,
                participantCount = participantCount
            )
        val remaining = budget.remaining()
        return AdminBudgetDto(
            budgetDate = budget.budgetDate,
            totalGranted = budget.totalGranted,
            totalLimit = budget.dailyLimit,
            remaining = remaining,
            participantCount = participantCount
        )
    }

    /**
     * 당일 "오늘 룰렛을 통해 발급 받을 수 있는 전체 포인트"를 수정.
     * 요청한 remaining(남은 예산)이 되도록 dailyLimit = totalGranted + remaining 으로 설정. totalGranted는 변경하지 않음.
     * @param remaining 설정할 남은 예산 (0 이상). 이미 지급액(totalGranted)보다 작게 설정 불가 → C016.
     */
    @Transactional
    fun patchTodayBudget(remaining: Long): AdminBudgetDto {
        require(remaining >= 0) { "잔여 예산은 0 이상이어야 합니다." }
        val today = LocalDate.now()
        val budget = getOrCreateSystemBudgetWithLock(today)
        val newTotalLimit = budget.totalGranted + remaining
        if (newTotalLimit < budget.totalGranted) {
            throw voltup.be.api.exception.BusinessException(
                voltup.be.api.exception.ErrorCode.BUDGET_CANNOT_DECREASE
            )
        }
        budget.dailyLimit = newTotalLimit
        budget.touch()
        val participantCount = dailyBudgetRepository.countByBudgetDateAndCancelledFalse(today)
        return AdminBudgetDto(
            budgetDate = budget.budgetDate,
            totalGranted = budget.totalGranted,
            totalLimit = budget.dailyLimit,
            remaining = budget.remaining(),
            participantCount = participantCount
        )
    }

    private fun getOrCreateSystemBudgetWithLock(today: LocalDate): SystemDailyBudget {
        systemDailyBudgetRepository.findByBudgetDateForUpdate(today)?.let { return it }
        try {
            systemDailyBudgetRepository.saveAndFlush(
                SystemDailyBudget(budgetDate = today, totalGranted = 0L, dailyLimit = SystemDailyBudget.DEFAULT_DAILY_LIMIT)
            )
        } catch (_: org.springframework.dao.DataIntegrityViolationException) { }
        return systemDailyBudgetRepository.findByBudgetDateForUpdate(today)!!
    }

    /** 어드민 예산 조회/수정 응답 DTO. */
    data class AdminBudgetDto(
        val budgetDate: LocalDate,
        /** 참여자들이 룰렛을 통해 받은 포인트 전체 합. */
        val totalGranted: Long,
        /** 오늘 룰렛을 통해 발급 가능한 전체 포인트 (어드민 수정 대상). */
        val totalLimit: Long,
        /** 전체 예산에서 참여자 지급분 제외한 남은 예산 (= totalLimit - totalGranted). */
        val remaining: Long,
        val participantCount: Long
    )
}
