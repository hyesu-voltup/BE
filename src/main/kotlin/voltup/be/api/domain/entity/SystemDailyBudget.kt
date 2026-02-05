package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 시스템 일일 예산 (전체 당일 지급 한도 10만 P).
 * 목적: 룰렛 참여 시 비관적 락으로 (오늘 총 지급 + 이번 당첨금) <= 100,000 보장.
 * 예) 잔여 1,000p일 때 5명이 동시에 500p 당첨 시도 → 락으로 정확히 예산 범위 내만 지급(선착 2명), 나머지는 꽝.
 */
@Entity
@Table(name = "system_daily_budgets")
class SystemDailyBudget(
    /** 예산 적용 일자 */
    @Column(name = "budget_date", nullable = false, unique = true)
    var budgetDate: LocalDate,

    /** 당일 누적 지급 포인트 (최대 100,000) */
    @Column(name = "total_granted", nullable = false)
    var totalGranted: Long = 0L
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        const val DAILY_LIMIT = 100_000L
    }

    /**
     * 이번 당첨금을 반영 가능한지 검사 후 반영 (도메인 로직).
     * @param amount 이번 지급 포인트
     * @throws voltup.be.api.exception.InsufficientBudgetException (totalGranted + amount) > DAILY_LIMIT 시
     */
    fun addGranted(amount: Long) {
        require(amount > 0) { "지급 포인트는 0보다 커야 합니다." }
        if (totalGranted + amount > DAILY_LIMIT) {
            throw voltup.be.api.exception.InsufficientBudgetException(totalGranted, amount, DAILY_LIMIT)
        }
        totalGranted += amount
        touch()
    }

    /** (totalGranted + amount) <= DAILY_LIMIT 여부. */
    fun canGrant(amount: Long): Boolean = totalGranted + amount <= DAILY_LIMIT

    /** 룰렛 취소 시 예산 반환 (0 미만으로 내려가지 않음). */
    fun subtractGranted(amount: Long) {
        require(amount > 0) { "반환 포인트는 0보다 커야 합니다." }
        totalGranted = (totalGranted - amount).coerceAtLeast(0)
        touch()
    }
}
