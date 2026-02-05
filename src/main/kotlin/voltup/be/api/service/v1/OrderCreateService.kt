package voltup.be.api.service.v1

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.Order
import voltup.be.api.domain.entity.OrderStatus
import voltup.be.api.domain.service.PointDomainService
import voltup.be.api.exception.InsufficientStockException
import voltup.be.api.exception.NotFoundException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.repository.OrderRepository
import voltup.be.api.repository.PointRepository
import voltup.be.api.repository.ProductRepository
import voltup.be.api.service.UserService

/**
 * 상품 구매 서비스 (v1).
 * 목적: 유저 포인트·상품 재고에 비관적 락으로 마이너스 잔고/재고 방지.
 */
@Service
class OrderCreateService(
    private val userService: UserService,
    private val pointRepository: PointRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val pointDomainService: PointDomainService
) {

    @Transactional
    fun order(userId: Long, productId: Long, quantity: Int): OrderCreateResult {
        userService.findUserById(userId)
        val point = pointRepository.findByUserIdForUpdate(userId)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_POINT)
        val product = productRepository.findByIdForUpdate(productId)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_PRODUCT)
        if (!product.hasStock(quantity)) {
            throw InsufficientStockException(product.stock, quantity)
        }
        val totalPoint = product.totalPointFor(quantity)
        pointDomainService.deduct(point, totalPoint)
        product.deductStock(quantity)
        val user = userService.findUserById(userId)
        val order = Order(
            user = user,
            product = product,
            quantity = quantity,
            pointAmount = totalPoint,
            status = OrderStatus.ORDERED
        )
        val saved = orderRepository.save(order)
        return OrderCreateResult(
            orderId = saved.id!!,
            pointAmount = saved.pointAmount,
            quantity = saved.quantity
        )
    }

    data class OrderCreateResult(
        val orderId: Long,
        val pointAmount: Long,
        val quantity: Int
    )
}
