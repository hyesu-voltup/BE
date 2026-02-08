package voltup.be.api.dto.admin

import io.swagger.v3.oas.annotations.media.Schema

/** 어드민 상품 수정 요청 (null 필드는 미변경) */
@Schema(description = "어드민 상품 수정 요청")
data class ProductUpdateRequest(
    @Schema(description = "상품명") val name: String? = null,
    @Schema(description = "1개당 포인트 가격") val pointPrice: Long? = null,
    @Schema(description = "재고 수량") val stock: Int? = null
)
