package voltup.be.api.controller.v1

import voltup.be.api.dto.v1.MyOrderItemResponse
import voltup.be.api.dto.v1.OrderCreateRequest
import voltup.be.api.dto.v1.OrderCreateResponse
import voltup.be.api.service.v1.OrderCreateService
import voltup.be.api.service.v1.OrderQueryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 주문 API (v1).
 * 목적: 상품 구매, 본인 주문 내역 조회.
 */
@Tag(name = "User API", description = "일반 사용자 기능")
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderCreateService: OrderCreateService,
    private val orderQueryService: OrderQueryService
) {

    @Operation(
        summary = "내 주문 내역",
        description = "본인의 구매 이력 (구매 품목명, 수량, 사용 포인트, 구매일)."
    )
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "성공")])
    @GetMapping("/me/{userId}")
    fun getMyOrders(
        @Parameter(description = "사용자 ID", required = true) @PathVariable userId: Long
    ): List<MyOrderItemResponse> = orderQueryService.getMyOrders(userId)

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
