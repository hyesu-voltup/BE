package voltup.be.api.controller.admin

import voltup.be.api.dto.admin.ProductUpdateRequest
import voltup.be.api.dto.response.ProductResponse
import voltup.be.api.service.admin.AdminProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 상품 수정 API.
 */
@Tag(name = "Admin API", description = "관리자 전용 기능")
@RestController
@RequestMapping("/api/v1/admin/products")
class AdminProductController(
    private val adminProductService: AdminProductService
) {

    @Operation(
        summary = "상품 수정",
        description = "상품명, 가격, 재고 등을 수정. 전달한 필드만 변경."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 완료"),
            ApiResponse(responseCode = "404", description = "상품 없음", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PutMapping("/{productId}")
    fun update(
        @Parameter(description = "상품 ID", required = true) @PathVariable productId: Long,
        @RequestBody request: ProductUpdateRequest
    ): ProductResponse {
        return adminProductService.update(productId, request.name, request.pointPrice, request.stock)
    }
}
