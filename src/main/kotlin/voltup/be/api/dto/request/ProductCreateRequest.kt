package voltup.be.api.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 상품 생성 요청 DTO.
 */
@Schema(description = "상품 생성 요청")
data class ProductCreateRequest(
    @Schema(description = "상품명", required = true)
    @field:NotBlank(message = "상품명은 필수입니다.")
    @field:Size(max = 200)
    val name: String,

    @Schema(description = "상품 1개당 포인트 가격", required = true)
    @field:Min(0, message = "포인트 가격은 0 이상이어야 합니다.")
    val pointPrice: Long,

    @Schema(description = "재고 수량")
    @field:Min(0, message = "재고는 0 이상이어야 합니다.")
    val stock: Int = 0
)
