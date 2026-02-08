package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.dto.admin.AdminOrderItemResponse
import voltup.be.api.repository.OrderRepository

/**
 * 어드민 전체 주문 조회 서비스.
 */
@Service
class AdminOrderQueryService(
    private val orderRepository: OrderRepository
) {

    @Transactional(readOnly = true)
    fun getAllOrders(): List<AdminOrderItemResponse> =
        orderRepository.findAllByOrderByCreatedAtDesc().map { o ->
            AdminOrderItemResponse(
                orderId = o.id!!,
                userId = o.user.id!!,
                nickname = o.user.name,
                orderedAt = o.createdAt,
                productName = o.product.name,
                quantity = o.quantity
            )
        }
}
