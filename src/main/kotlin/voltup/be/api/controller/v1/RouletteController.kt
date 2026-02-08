package voltup.be.api.controller.v1

import voltup.be.api.dto.v1.RouletteParticipateResponse
import voltup.be.api.service.v1.RouletteParticipateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 룰렛 API (v1).
 * 목적: 당일 1인 1회 참여, 당첨 포인트 지급 및 예산 차감. SystemDailyBudget·DailyBudget 비관적 락 적용.
 */
@Tag(name = "User API", description = "일반 사용자 기능")
@RestController
@RequestMapping("/api/v1/roulette")
class RouletteController(
    private val rouletteParticipateService: RouletteParticipateService
) {

    @Operation(
        summary = "룰렛 돌리기",
        description = "당일 1인 1회 참여. 당첨 시 100P~1000P 범위에서 랜덤 지급. " +
            "같은 유저가 동시에 여러 번 호출해도 1회만 성공(나머지 409). " +
            "일일 예산(10만 P) 비관적 락으로 정확히 예산 범위 내에서만 지급(예: 잔여 1,000P일 때 500P 5명 동시 요청 → 2명만 지급, 나머지 꽝)."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "참여 성공"),
            ApiResponse(responseCode = "400", description = "일일 예산 초과 (꽝)", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))]),
            ApiResponse(responseCode = "404", description = "사용자/포인트 계정 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))]),
            ApiResponse(responseCode = "409", description = "당일 이미 참여함", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PostMapping("/participate")
    @ResponseStatus(HttpStatus.OK)
    fun participate(
        @Parameter(description = "현재 로그인 사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: Long
    ): RouletteParticipateResponse {
        val result = rouletteParticipateService.participate(userId)
        return RouletteParticipateResponse(
            grantedPoint = result.grantedPoint,
            balanceAfter = result.balanceAfter,
            participationId = result.participationId
        )
    }
}
