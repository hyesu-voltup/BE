package voltup.be.api.dto.v1

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/** 간편 로그인 요청 (닉네임 또는 아이디만 입력) */
@Schema(description = "간편 로그인 요청")
data class LoginRequest(
    @Schema(description = "닉네임 또는 아이디", required = true)
    @field:NotBlank(message = "닉네임은 필수입니다.")
    val nickname: String
)
