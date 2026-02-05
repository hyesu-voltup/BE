package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 내 포인트 현황 응답 (v1).
 */
@Schema(description = "내 포인트 현황")
data class PointsMeResponse(
    @Schema(description = "가용 잔액 (P)", example = "1000") val availableBalance: Long,
    @Schema(description = "7일 이내 만료 예정 포인트 (P)", example = "100") val expiringWithin7Days: Long
)
