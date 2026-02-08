package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/** 어드민 전체 주문 한 건 */
@Schema(description = "전체 주문 항목")
data class AdminOrderItemResponse(
    @Schema(description = "주문 ID") val orderId: Long,
    @Schema(description = "유저 ID") val userId: Long,
    @Schema(description = "유저 닉네임") val nickname: String,
    @Schema(description = "주문 시간") val orderedAt: LocalDateTime,
    @Schema(description = "물품명") val productName: String,
    @Schema(description = "수량") val quantity: Int
)
