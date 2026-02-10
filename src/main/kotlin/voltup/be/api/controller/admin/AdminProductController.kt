package voltup.be.api.controller.admin

import voltup.be.api.dto.admin.ProductUpdateRequest
import voltup.be.api.dto.request.ProductCreateRequest
import voltup.be.api.dto.response.ProductResponse
import voltup.be.api.service.ProductService
import voltup.be.api.service.admin.AdminProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 상품 API.
 * 목적: 상품 등록(어드민 전용), 상품 수정.
 */
@Tag(name = "Admin API", description = "관리자 전용 기능")
@RestController
@RequestMapping("/api/v1/admin/products")
class AdminProductController(
    private val productService: ProductService,
    private val adminProductService: AdminProductService
) {

    @Operation(
        summary = "상품 등록",
        description = "상품명·포인트 가격·재고로 상품 생성. 어드민 전용."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "생성됨"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 값", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: ProductCreateRequest): ProductResponse {
        return productService.create(request)
    }

    @Operation(
        summary = "상품 삭제",
        description = "상품 소프트 삭제. 삭제된 상품은 목록/조회에서 제외됨."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 완료"),
            ApiResponse(responseCode = "404", description = "상품 없음 또는 이미 삭제됨", content = [Content(schema = Schema(implementation = voltup.be.api.exception.ErrorResponse::class))])
        ]
    )
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@Parameter(description = "상품 ID", required = true) @PathVariable productId: Long) {
        adminProductService.delete(productId)
    }

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
