package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

/**
 * 사용자별 포인트 계정(잔액) 엔티티.
 * 목적: 1인 1포인트 계정, 잔액 보유 및 차감/적립 시 비관적 락으로 정합성 유지.
 */
@Entity
@Table(name = "points")
class Point(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User,

    @Column(nullable = false)
    var balance: Long = 0L
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    /**
     * 포인트 차감 가능 여부 및 차감 수행 (도메인 로직).
     * @param amount 차감할 포인트 (0 초과)
     * @return 차감 후 남은 잔액
     * @throws voltup.be.api.exception.InsufficientPointException 잔액 부족 시
     */
    fun deduct(amount: Long): Long {
        require(amount > 0) { "차감 포인트는 0보다 커야 합니다." }
        if (balance < amount) {
            throw voltup.be.api.exception.InsufficientPointException(balance, amount)
        }
        balance -= amount
        touch()
        return balance
    }

    /**
     * 포인트 적립.
     * @param amount 적립할 포인트 (0 초과)
     * @return 적립 후 잔액
     */
    fun add(amount: Long): Long {
        require(amount > 0) { "적립 포인트는 0보다 커야 합니다." }
        balance += amount
        touch()
        return balance
    }

    /** 잔액이 amount 이상인지 여부. */
    fun hasEnough(amount: Long): Boolean = balance >= amount

    /**
     * 만료 처리용 차감 (잔액을 0 미만으로 만들지 않음).
     * @param amount 만료할 포인트
     * @return 차감 후 잔액
     */
    fun deductForExpiry(amount: Long): Long {
        require(amount > 0) { "만료 차감 포인트는 0보다 커야 합니다." }
        val deduct = minOf(amount, balance)
        balance -= deduct
        touch()
        return balance
    }
}
