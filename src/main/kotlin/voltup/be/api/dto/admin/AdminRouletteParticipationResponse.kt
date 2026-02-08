package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/** 어드민 룰렛 참여 기록 한 건 (참여 취소/회수용) */
@Schema(description = "룰렛 참여 기록")
data class AdminRouletteParticipationResponse(
    @Schema(description = "참여 ID") val participationId: Long,
    @Schema(description = "사용자 ID") val userId: Long,
    @Schema(description = "유저 닉네임") val nickname: String,
    @Schema(description = "참여 시간") val participatedAt: LocalDateTime
)
