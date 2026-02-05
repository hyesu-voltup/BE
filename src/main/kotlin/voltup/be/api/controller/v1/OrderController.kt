package voltup.be.api.controller.v1

import voltup.be.api.dto.v1.OrderCreateRequest
import voltup.be.api.dto.v1.OrderCreateResponse
import voltup.be.api.service.v1.OrderCreateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 주문 API (v1).
 * 목적: 상품 구매. 유저 포인트·상품 재고에 비관적 락으로 마이너스 잔고/재고 방지.
 */
@Tag(name = "Order (v1)", description = "상품 구매 API")
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderCreateService: OrderCreateService
) {

    @Operation(
        summary = "상품 구매",
        description = "포인트 차감 및 상품 재고 감소. 유저 포인트와 상품 재고에 동시에 비관적 락 적용."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "주문 생성됨"),
            ApiResponse(responseCode = "400", description = "포인트 부족 또는 재고 부족", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))]),
            ApiResponse(responseCode = "404", description = "사용자/상품/포인트 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Parameter(description = "현재 로그인 사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: OrderCreateRequest
    ): OrderCreateResponse {
        val result = orderCreateService.order(userId, request.productId, request.quantity)
        return OrderCreateResponse(
            orderId = result.orderId,
            pointAmount = result.pointAmount,
            quantity = result.quantity
        )
    }
}
