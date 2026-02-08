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
import voltup.be.api.dto.request.UserCreateRequest
import voltup.be.api.dto.response.UserResponse
import voltup.be.api.service.UserService

/**
 * 사용자 API.
 * 목적: 사용자 생성·조회 REST 엔드포인트.
 */
@Tag(name = "User API", description = "일반 사용자 기능")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "사용자 생성", description = "로그인 ID·이름으로 사용자 및 포인트 계정 생성 (1인 1계정)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: UserCreateRequest): UserResponse {
        return userService.create(request)
    }

    @Operation(summary = "사용자 조회", description = "ID로 사용자 정보 조회")
    @GetMapping("/{userId}")
    fun getById(@PathVariable userId: Long): UserResponse {
        return userService.getById(userId)
    }
}
