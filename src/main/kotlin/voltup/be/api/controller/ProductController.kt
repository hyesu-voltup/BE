package voltup.be.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import voltup.be.api.dto.request.ProductCreateRequest
import voltup.be.api.dto.response.ProductResponse
import voltup.be.api.service.ProductService

/**
 * 상품 API.
 * 목적: 전체 상품 목록·등록·단건 조회.
 */
@Tag(name = "User API", description = "일반 사용자 기능")
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService
) {

    @Operation(
        summary = "전체 상품 목록",
        description = "모든 사용자가 볼 수 있도록 상품 ID, 상품명, 가격, 재고 리스트 반환."
    )
    @GetMapping
    fun getAll(): List<ProductResponse> = productService.getAll()

    @Operation(summary = "상품 등록", description = "상품명·포인트 가격·재고로 상품 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: ProductCreateRequest): ProductResponse {
        return productService.create(request)
    }

    @Operation(summary = "상품 조회", description = "ID로 상품 정보 조회")
    @GetMapping("/{productId}")
    fun getById(@PathVariable productId: Long): ProductResponse {
        return productService.getById(productId)
    }
}
