package voltup.be.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 룰렛 참여 결과 응답 DTO.
 * 목적: 당일 1회 참여 시 지급된 포인트 및 잔액 반환.
 */
@Schema(description = "룰렛 참여 결과 응답")
data class RouletteResultResponse(
    @Schema(description = "지급된 포인트") val grantedPoint: Long,
    @Schema(description = "참여 후 포인트 잔액") val balanceAfter: Long
)
