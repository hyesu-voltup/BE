package voltup.be.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import voltup.be.api.domain.entity.Order

/**
 * 주문 응답 DTO.
 */
@Schema(description = "주문 정보 응답")
data class OrderResponse(
    @Schema(description = "주문 ID") val id: Long,
    @Schema(description = "사용자 ID") val userId: Long,
    @Schema(description = "상품 ID") val productId: Long,
    @Schema(description = "주문 수량") val quantity: Int,
    @Schema(description = "결제 포인트") val pointAmount: Long
) {
    companion object {
        fun from(entity: Order): OrderResponse = OrderResponse(
            id = entity.id!!,
            userId = entity.user.id!!,
            productId = entity.product.id!!,
            quantity = entity.quantity,
            pointAmount = entity.pointAmount
        )
    }
}
