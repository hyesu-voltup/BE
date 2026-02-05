package voltup.be.api.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

/**
 * 상품 주문 요청 DTO.
 * 목적: 상품 ID, 수량 검증 및 서비스 전달.
 */
@Schema(description = "상품 주문 요청")
data class OrderCreateRequest(
    @Schema(description = "상품 ID", required = true)
    @field:NotNull(message = "상품 ID는 필수입니다.")
    val productId: Long,

    @Schema(description = "주문 수량", example = "1", required = true)
    @field:NotNull
    @field:Min(1, message = "수량은 1 이상이어야 합니다.")
    val quantity: Int
)
