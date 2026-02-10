package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/** 포인트 히스토리 한 건. statusMessage는 관리자 수거/거부 시에만 값 있음. */
@Schema(description = "포인트 히스토리 항목")
data class PointHistoryItemResponse(
    @Schema(description = "내역 설명 (예: 룰렛 당첨, 주문 취소 환불)") val description: String,
    @Schema(description = "포인트 금액") val amount: Long,
    @Schema(description = "유효기간(만료 예정일). 수거된 건은 null") val expiryDate: LocalDate?,
    @Schema(description = "상태 메시지. 정상이면 null, 관리자 수거 시 '관리자에 의해 수거되었습니다'") val statusMessage: String? = null
)
