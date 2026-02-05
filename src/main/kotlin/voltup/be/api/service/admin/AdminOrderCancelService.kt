package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.service.PointDomainService
import voltup.be.api.exception.NotFoundException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.repository.OrderRepository
import voltup.be.api.repository.PointRepository

/**
 * 어드민 주문 취소 서비스.
 * 목적: 주문 상태 변경, 유저 포인트 환불(보상 트랜잭션), 상품 재고 복원.
 */
@Service
class AdminOrderCancelService(
    private val orderRepository: OrderRepository,
    private val pointRepository: PointRepository,
    private val pointDomainService: PointDomainService
) {

    @Transactional
    fun cancelOrder(orderId: Long) {
        val order = orderRepository.findByIdForUpdate(orderId)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_ORDER)
        order.cancel()
        val point = pointRepository.findByUserIdForUpdate(order.user.id!!)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_POINT)
        pointDomainService.refund(point, order.pointAmount, order.id!!)
        order.product.addStock(order.quantity)
    }
}
