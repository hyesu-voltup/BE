package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 룰렛 상태 조회 응답 (일반 사용자).
 * 목적: 당일 룰렛 일일 예산 잔여량, 당일 참여 여부 제공.
 */
@Schema(description = "룰렛 상태 (남은 예산, 참여 여부)")
data class RouletteStatusResponse(
    @Schema(description = "오늘 룰렛 일일 예산 잔여 포인트 (P). 0이면 당첨 불가(꽝).") val remainingBudget: Long,
    @Schema(description = "당일 이미 참여 여부. true면 오늘은 더 이상 참여 불가.") val alreadyParticipated: Boolean
)
