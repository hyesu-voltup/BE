package voltup.be.api.controller.v1

import voltup.be.api.dto.v1.LoginRequest
import voltup.be.api.dto.v1.LoginResponse
import voltup.be.api.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 인증 API (간편 로그인).
 * 목적: 닉네임만 입력받아 로그인 처리, userId 반환으로 이후 요청 식별자 제공.
 */
@Tag(name = "User API", description = "일반 사용자 기능")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService
) {

    @Operation(
        summary = "간편 로그인",
        description = "닉네임(또는 아이디)만 입력받아 로그인. 없으면 자동 생성 후 userId 반환."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "성공 (userId로 이후 요청 식별)")
        ]
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val user = userService.loginOrCreate(request.nickname)
        return LoginResponse(userId = user.id!!, nickname = user.name)
    }
}
