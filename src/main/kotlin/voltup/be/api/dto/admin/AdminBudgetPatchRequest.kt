package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@Schema(description = "어드민 일일 예산 수정 요청. 설정할 남은 예산(remaining)을 보내면, 오늘 발급 가능 전체 포인트(totalLimit)가 totalGranted + remaining 으로 설정됨.")
data class AdminBudgetPatchRequest(
    @Schema(description = "설정할 당일 남은 예산 (0 이상). 이미 지급액(totalGranted)보다 적게 설정 시 C016.", required = true)
    @field:Min(0)
    val remaining: Long
)
