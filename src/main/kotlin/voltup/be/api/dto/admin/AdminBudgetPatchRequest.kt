package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@Schema(description = "어드민 일일 예산 강제 설정 요청 (잔여 예산 기준)")
data class AdminBudgetPatchRequest(
    @Schema(description = "강제로 설정할 당일 잔여 예산 (0 ~ 100,000). 이미 지급액보다 적게 설정 시 C016 에러.", required = true)
    @field:Min(0) @field:Max(100_000)
    val remaining: Long
)
