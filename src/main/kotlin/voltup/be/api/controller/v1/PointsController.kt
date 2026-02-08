package voltup.be.api.controller.v1

import voltup.be.api.dto.v1.PointsDetailResponse
import voltup.be.api.dto.v1.PointsMeResponse
import voltup.be.api.service.v1.PointsMeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 포인트 API (v1).
 * 목적: 내 포인트 현황·상세 (총 잔액, 7일 만료 예정, 히스토리). 조회 시점 만료 반영.
 */
@Tag(name = "User API", description = "일반 사용자 기능")
@RestController
@RequestMapping("/api/v1/points")
class PointsController(
    private val pointsMeService: PointsMeService
) {

    @Operation(
        summary = "내 포인트 현황 (요약)",
        description = "가용 잔액 및 7일 이내 만료 예정 포인트. 포인트 유효기간은 획득일로부터 30일(만료 포인트는 사용 불가)."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "성공"),
            ApiResponse(responseCode = "404", description = "포인트 계정 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @GetMapping("/me")
    fun me(
        @Parameter(description = "현재 로그인 사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: Long
    ): PointsMeResponse {
        val result = pointsMeService.getMyPoints(userId)
        return PointsMeResponse(
            availableBalance = result.availableBalance,
            expiringWithin7Days = result.expiringWithin7Days
        )
    }

    @Operation(
        summary = "내 포인트 상세 조회",
        description = "총 잔액(만료 반영) + 유효한 포인트 히스토리(내역 설명, 금액, 만료 예정일)."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "성공"),
            ApiResponse(responseCode = "404", description = "포인트 계정 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @GetMapping("/me/{userId}")
    fun detail(
        @Parameter(description = "사용자 ID", required = true) @PathVariable userId: Long
    ): PointsDetailResponse {
        return pointsMeService.getMyPointDetail(userId)
    }
}
