package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 포인트 히스토리 한 건 (유효한 획득 내역) */
@Schema(description = "포인트 히스토리 항목")
data class PointHistoryItemResponse(
    @Schema(description = "내역 설명 (예: 룰렛 당첨, 주문 취소 환불)") val description: String,
    @Schema(description = "포인트 금액") val amount: Long,
    @Schema(description = "유효기간(만료 예정일)") val expiryDate: LocalDate?
)
