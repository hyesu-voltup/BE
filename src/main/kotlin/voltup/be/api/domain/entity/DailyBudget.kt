package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

/**
 * 유저별 일별 룰렛 참여/예산 엔티티.
 * 목적: 1인 1회 참여 제한을 DB 복합 유니크(userId, date)로 보장.
 */
@Entity
@Table(
    name = "daily_budgets",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "budget_date"])
    ]
)
class DailyBudget(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    /** 참여/예산 적용 일자 */
    @Column(name = "budget_date", nullable = false)
    var budgetDate: LocalDate,

    @Column(name = "granted_point", nullable = false)
    var grantedPoint: Long = 0L,

    @Column(nullable = false)
    var cancelled: Boolean = false
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    /**
     * 당일 지급 포인트 추가 (도메인 로직).
     * @param amount 지급 포인트
     */
    fun addGrantedPoint(amount: Long) {
        require(amount > 0) { "지급 포인트는 0보다 커야 합니다." }
        grantedPoint += amount
        touch()
    }

    /** 어드민 룰렛 취소 시 호출. 중복 취소 방지. */
    fun cancel() {
        if (cancelled) {
            throw voltup.be.api.exception.BusinessException(
                voltup.be.api.exception.ErrorCode.PARTICIPATION_ALREADY_CANCELLED
            )
        }
        cancelled = true
        touch()
    }
}
