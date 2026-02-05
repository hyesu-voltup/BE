package voltup.be.api.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 사용자 생성 요청 DTO.
 * 목적: API 요청 검증 및 서비스 전달.
 */
@Schema(description = "사용자 생성 요청")
data class UserCreateRequest(
    @Schema(description = "로그인 ID", example = "user01", required = true)
    @field:NotBlank(message = "로그인 ID는 필수입니다.")
    @field:Size(max = 100)
    val loginId: String,

    @Schema(description = "이름", example = "홍길동", required = true)
    @field:NotBlank(message = "이름은 필수입니다.")
    @field:Size(max = 100)
    val name: String
)
