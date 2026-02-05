package voltup.be.api.controller.admin

import voltup.be.api.service.admin.AdminOrderCancelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 주문 API.
 * 목적: 주문 취소(상태 변경 + 포인트 환불 + 재고 복원).
 */
@Tag(name = "Admin Order", description = "어드민 주문 취소 API")
@RestController
@RequestMapping("/api/v1/admin/orders")
class AdminOrderController(
    private val adminOrderCancelService: AdminOrderCancelService
) {

    @Operation(
        summary = "주문 취소",
        description = "주문 상태를 CANCELLED로 변경하고, 유저 포인트 환불(보상 트랜잭션), 상품 재고 복원."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 완료"),
            ApiResponse(responseCode = "404", description = "주문 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))]),
            ApiResponse(responseCode = "409", description = "이미 취소된 주문", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @Parameter(description = "주문 ID", required = true) @PathVariable orderId: Long
    ) {
        adminOrderCancelService.cancelOrder(orderId)
    }
}
