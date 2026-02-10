package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 시스템 일일 예산.
 * 목적: 룰렛 참여 시 비관적 락으로 (오늘 총 지급 + 이번 당첨금) <= dailyLimit 보장.
 * - totalGranted: 참여자들이 룰렛을 통해 받은 포인트 전체 합 (지급/취소 시에만 변경).
 * - dailyLimit: 오늘 룰렛을 통해 발급 가능한 전체 포인트 (어드민 수정 가능).
 * - remaining = dailyLimit - totalGranted (계산값).
 *
 * 배포 시: system_daily_budgets 테이블에 daily_limit 컬럼 추가 필요.
 * 기존 DB) ALTER TABLE system_daily_budgets ADD COLUMN daily_limit BIGINT NOT NULL DEFAULT 100000;
 */
@Entity
@Table(name = "system_daily_budgets")
class SystemDailyBudget(
    /** 예산 적용 일자 */
    @Column(name = "budget_date", nullable = false, unique = true)
    var budgetDate: LocalDate,

    /** 당일 누적 지급 포인트 (참여자들이 룰렛을 통해 받은 포인트 전체 합). 지급/취소 시에만 변경. */
    @Column(name = "total_granted", nullable = false)
    var totalGranted: Long = 0L,

    /** 오늘 룰렛을 통해 발급 가능한 전체 포인트. 어드민 예산 수정 시 이 값만 변경. */
    @Column(name = "daily_limit", nullable = false)
    var dailyLimit: Long = DEFAULT_DAILY_LIMIT
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        /** 신규 생성 시 기본 한도 (P). */
        const val DEFAULT_DAILY_LIMIT = 100_000L
    }

    /**
     * 이번 당첨금을 반영 가능한지 검사 후 반영 (도메인 로직).
     * @param amount 이번 지급 포인트
     * @throws voltup.be.api.exception.InsufficientBudgetException (totalGranted + amount) > dailyLimit 시
     */
    fun addGranted(amount: Long) {
        require(amount > 0) { "지급 포인트는 0보다 커야 합니다." }
        if (totalGranted + amount > dailyLimit) {
            throw voltup.be.api.exception.InsufficientBudgetException(totalGranted, amount, dailyLimit)
        }
        totalGranted += amount
        touch()
    }

    /** (totalGranted + amount) <= dailyLimit 여부. */
    fun canGrant(amount: Long): Boolean = totalGranted + amount <= dailyLimit

    /** 남은 예산 = 전체 발급 한도 - 이미 지급한 합계. */
    fun remaining(): Long = (dailyLimit - totalGranted).coerceAtLeast(0)

    /** 룰렛 취소 시 예산 반환 (0 미만으로 내려가지 않음). */
    fun subtractGranted(amount: Long) {
        require(amount > 0) { "반환 포인트는 0보다 커야 합니다." }
        totalGranted = (totalGranted - amount).coerceAtLeast(0)
        touch()
    }
}
