package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema

/** 간편 로그인 응답 (프론트엔드가 이후 요청에서 식별자로 사용) */
@Schema(description = "간편 로그인 응답")
data class LoginResponse(
    @Schema(description = "사용자 ID (이후 API 식별자로 사용)") val userId: Long,
    @Schema(description = "닉네임") val nickname: String
)
