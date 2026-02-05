package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 상품 구매 응답 (v1).
 */
@Schema(description = "상품 구매 결과")
data class OrderCreateResponse(
    @Schema(description = "주문 ID") val orderId: Long,
    @Schema(description = "결제 포인트") val pointAmount: Long,
    @Schema(description = "주문 수량") val quantity: Int
)
