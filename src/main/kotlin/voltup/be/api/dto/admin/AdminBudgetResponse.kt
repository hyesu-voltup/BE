package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "어드민 일일 예산 응답")
data class AdminBudgetResponse(
    @Schema(description = "예산 일자") val budgetDate: LocalDate,
    @Schema(description = "당일 누적 지급 포인트") val totalGranted: Long,
    @Schema(description = "잔여 예산 (100,000 - totalGranted)") val remaining: Long
)
