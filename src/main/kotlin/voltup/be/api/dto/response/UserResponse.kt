package voltup.be.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import voltup.be.api.domain.entity.User

/**
 * 사용자 응답 DTO.
 * 목적: API 응답 시 엔티티 노출 최소화.
 */
@Schema(description = "사용자 정보 응답")
data class UserResponse(
    @Schema(description = "사용자 ID") val id: Long,
    @Schema(description = "로그인 ID") val loginId: String,
    @Schema(description = "이름") val name: String
) {
    companion object {
        fun from(entity: User): UserResponse = UserResponse(
            id = entity.id!!,
            loginId = entity.loginId,
            name = entity.name
        )
    }
}
