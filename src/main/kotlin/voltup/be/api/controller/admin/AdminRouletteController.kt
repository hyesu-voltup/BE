package voltup.be.api.controller.admin

import voltup.be.api.dto.admin.AdminRouletteParticipationResponse
import voltup.be.api.service.admin.AdminRouletteCancelService
import voltup.be.api.service.admin.AdminRouletteQueryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 룰렛 API.
 * 목적: 참여 기록 목록, 참여 취소(지급 포인트 회수).
 */
@Tag(name = "Admin API", description = "관리자 전용 기능")
@RestController
@RequestMapping("/api/v1/admin/roulette")
class AdminRouletteController(
    private val adminRouletteCancelService: AdminRouletteCancelService,
    private val adminRouletteQueryService: AdminRouletteQueryService
) {

    @Operation(
        summary = "룰렛 참여 기록 목록",
        description = "참여 취소(회수)를 위해 participationId, userId, 유저 닉네임, 참여 시간 제공."
    )
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "성공")])
    @GetMapping("/participations")
    fun getParticipations(): List<AdminRouletteParticipationResponse> =
        adminRouletteQueryService.getParticipations()

    @Operation(
        summary = "룰렛 참여 취소",
        description = "지급된 포인트를 회수하고 참여를 취소 처리. 유저 잔액이 부족하면 C015 예외."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 완료"),
            ApiResponse(responseCode = "400", description = "포인트 부족으로 회수 불가", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))]),
            ApiResponse(responseCode = "404", description = "참여 내역 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))]),
            ApiResponse(responseCode = "409", description = "이미 취소된 참여", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PostMapping("/{participationId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @Parameter(description = "참여 ID (룰렛 참여 시 반환된 participationId)", required = true)
        @PathVariable participationId: Long
    ) {
        adminRouletteCancelService.cancelParticipation(participationId)
    }
}
