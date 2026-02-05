package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

/**
 * 상품 구매 요청 (v1).
 */
@Schema(description = "상품 구매 요청")
data class OrderCreateRequest(
    @Schema(description = "상품 ID", required = true) @field:NotNull val productId: Long,
    @Schema(description = "수량", required = true, example = "1")
    @field:NotNull
    @field:Min(1)
    val quantity: Int
)
