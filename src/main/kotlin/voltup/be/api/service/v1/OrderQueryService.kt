package voltup.be.api.service.v1

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.dto.v1.MyOrderItemResponse
import voltup.be.api.repository.OrderRepository
import voltup.be.api.service.UserService

/**
 * 주문 조회 서비스 (v1).
 * 목적: 본인 주문 내역 조회 등 읽기 전용.
 */
@Service
class OrderQueryService(
    private val userService: UserService,
    private val orderRepository: OrderRepository
) {

    @Transactional(readOnly = true)
    fun getMyOrders(userId: Long): List<MyOrderItemResponse> {
        val user = userService.findUserById(userId)
        val orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.id!!)
        return orders.map { o ->
            MyOrderItemResponse(
                orderId = o.id!!,
                productName = o.product.name,
                quantity = o.quantity,
                usedPoint = o.pointAmount,
                orderedAt = o.createdAt,
                status = o.status.name
            )
        }
    }
}
