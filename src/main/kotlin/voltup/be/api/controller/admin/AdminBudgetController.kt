package voltup.be.api.controller.admin

import voltup.be.api.dto.admin.AdminBudgetPatchRequest
import voltup.be.api.dto.admin.AdminBudgetResponse
import voltup.be.api.service.admin.AdminBudgetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 일일 예산 API.
 * 목적: 오늘 예산 잔액 확인 및 강제 설정.
 */
@Tag(name = "Admin Budget", description = "어드민 일일 예산 관리 API")
@RestController
@RequestMapping("/api/v1/admin/budget")
class AdminBudgetController(
    private val adminBudgetService: AdminBudgetService
) {

    @Operation(
        summary = "오늘 예산 조회",
        description = "당일 누적 지급액 및 잔여 예산(10만 P 한도) 확인."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "성공")
        ]
    )
    @GetMapping
    fun getBudget(): AdminBudgetResponse {
        val dto = adminBudgetService.getTodayBudget()
        return AdminBudgetResponse(
            budgetDate = dto.budgetDate,
            totalGranted = dto.totalGranted,
            remaining = dto.remaining
        )
    }

    @Operation(
        summary = "오늘 예산 강제 설정",
        description = "당일 총 지급액을 강제로 설정. (0 ~ 100,000)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "설정 완료"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 값", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PatchMapping
    fun patchBudget(@Valid @RequestBody request: AdminBudgetPatchRequest): AdminBudgetResponse {
        val dto = adminBudgetService.patchTodayBudget(request.totalGranted)
        return AdminBudgetResponse(
            budgetDate = dto.budgetDate,
            totalGranted = dto.totalGranted,
            remaining = dto.remaining
        )
    }
}
