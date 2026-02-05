package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * 주문 엔티티 (상품 구매).
 * 목적: 사용자-상품-포인트 결제 이력. 취소 시 상태 변경 및 환불.
 */
@Entity
@Table(name = "orders")
class Order(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int,

    @Column(name = "point_amount", nullable = false)
    var pointAmount: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.ORDERED
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    /** 주문 취소 처리 (도메인 로직). 이미 취소 시 예외. */
    fun cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw voltup.be.api.exception.BusinessException(
                voltup.be.api.exception.ErrorCode.ORDER_ALREADY_CANCELLED
            )
        }
        status = OrderStatus.CANCELLED
        touch()
    }

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED
}
