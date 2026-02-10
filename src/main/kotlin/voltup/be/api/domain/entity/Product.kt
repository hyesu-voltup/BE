package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

/**
 * 상품 엔티티 (룰렛 당첨 상품 또는 구매 상품).
 * 목적: 포인트 가격, 재고 보유. 주문 시 비관적 락으로 재고 정합성 유지.
 */
@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false, length = 200)
    var name: String,

    /** 상품 1개당 필요 포인트 */
    @Column(name = "point_price", nullable = false)
    var pointPrice: Long,

    /** 재고 수량 (0 이상) */
    @Column(nullable = false)
    var stock: Int = 0,

    /** 어드민 삭제 여부. true면 목록/조회에서 제외. */
    @Column(nullable = false)
    var deleted: Boolean = false
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    /**
     * 재고 차감 (도메인 로직). 동시성은 Repository 비관적 락으로 보장.
     * @param quantity 차감할 수량
     * @throws IllegalStateException 재고 부족 시
     */
    fun deductStock(quantity: Int) {
        require(quantity > 0) { "수량은 0보다 커야 합니다." }
        if (stock < quantity) {
            throw IllegalStateException("재고 부족: 현재=$stock, 요청=$quantity")
        }
        stock -= quantity
        touch()
    }

    /** 주문 시 필요한 총 포인트. */
    fun totalPointFor(quantity: Int): Long = pointPrice * quantity

    /** 재고 보유 여부 (quantity 이상). */
    fun hasStock(quantity: Int): Boolean = stock >= quantity

    /** 주문 취소 시 재고 복원 (도메인 로직). */
    fun addStock(quantity: Int) {
        require(quantity > 0) { "복원 수량은 0보다 커야 합니다." }
        stock += quantity
        touch()
    }
}
