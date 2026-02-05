package voltup.be.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import voltup.be.api.domain.entity.Product

/**
 * 상품 응답 DTO.
 */
@Schema(description = "상품 정보 응답")
data class ProductResponse(
    @Schema(description = "상품 ID") val id: Long,
    @Schema(description = "상품명") val name: String,
    @Schema(description = "1개당 포인트 가격") val pointPrice: Long,
    @Schema(description = "재고 수량") val stock: Int
) {
    companion object {
        fun from(entity: Product): ProductResponse = ProductResponse(
            id = entity.id!!,
            name = entity.name,
            pointPrice = entity.pointPrice,
            stock = entity.stock
        )
    }
}
