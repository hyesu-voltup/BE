package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/** 사용자 주문 내역 한 건. status: ORDERED=승인(기본), CANCELLED=어드민에 의해 취소(상품 회수). */
@Schema(description = "내 주문 내역 항목")
data class MyOrderItemResponse(
    @Schema(description = "주문 ID") val orderId: Long,
    @Schema(description = "구매 품목명") val productName: String,
    @Schema(description = "수량") val quantity: Int,
    @Schema(description = "사용 포인트") val usedPoint: Long,
    @Schema(description = "구매일") val orderedAt: LocalDateTime,
    @Schema(description = "주문 상태. ORDERED=승인, CANCELLED=취소(관리자 회수)") val status: String
)
