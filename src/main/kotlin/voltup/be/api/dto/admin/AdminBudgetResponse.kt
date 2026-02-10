package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "어드민 일일 예산 응답")
data class AdminBudgetResponse(
    @Schema(description = "예산 일자") val budgetDate: LocalDate,
    @Schema(description = "참여자들이 룰렛을 통해 받은 포인트 전체 합") val totalGranted: Long,
    @Schema(description = "오늘 룰렛을 통해 발급 가능한 전체 포인트 (어드민 수정 대상)") val totalLimit: Long,
    @Schema(description = "전체 예산에서 참여자 지급분 제외한 남은 예산 (totalLimit - totalGranted)") val remaining: Long,
    @Schema(description = "당일 참여 사용자 수 (포인트 받은 인원)") val participantCount: Long
)
