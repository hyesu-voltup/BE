package voltup.be.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import voltup.be.api.domain.entity.Point

/**
 * 포인트 잔액 응답 DTO.
 */
@Schema(description = "포인트 잔액 응답")
data class PointResponse(
    @Schema(description = "포인트 계정 ID") val id: Long,
    @Schema(description = "사용자 ID") val userId: Long,
    @Schema(description = "잔액") val balance: Long
) {
    companion object {
        fun from(entity: Point): PointResponse = PointResponse(
            id = entity.id!!,
            userId = entity.user.id!!,
            balance = entity.balance
        )
    }
}
