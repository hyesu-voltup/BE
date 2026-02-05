package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 룰렛 참여 결과 응답 (v1).
 */
@Schema(description = "룰렛 참여 결과 (당첨금 100P~1000P 랜덤)")
data class RouletteParticipateResponse(
    @Schema(description = "당첨 지급 포인트 (100~1000P)", example = "500") val grantedPoint: Long,
    @Schema(description = "참여 후 포인트 잔액", example = "150") val balanceAfter: Long,
    @Schema(description = "참여 ID (룰렛 취소 시 사용)", example = "1") val participationId: Long
)
