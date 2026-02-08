package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema

/** 내 포인트 상세 조회 응답 (총 잔액 + 유효한 획득/사용 히스토리) */
@Schema(description = "내 포인트 상세 (총 잔액, 포인트 히스토리)")
data class PointsDetailResponse(
    @Schema(description = "총 잔액 (만료 반영 후 유효 포인트 합계)") val totalBalance: Long,
    @Schema(description = "유효한 획득 내역 리스트 (만료되지 않은 건)") val histories: List<PointHistoryItemResponse>
)
