package voltup.be.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.Point
import voltup.be.api.domain.entity.User
import voltup.be.api.dto.request.UserCreateRequest
import voltup.be.api.dto.response.UserResponse
import voltup.be.api.exception.ErrorCode
import voltup.be.api.exception.NotFoundException
import voltup.be.api.repository.PointRepository
import voltup.be.api.repository.UserRepository

/**
 * 사용자 생성·조회 서비스.
 * 목적: 트랜잭션 경계 및 도메인 조합. 생성 시 포인트 계정 1:1 생성.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val pointRepository: PointRepository
) {

    /**
     * 사용자 생성 + 포인트 계정 생성 (1인 1계정).
     * @return 생성된 사용자 응답
     */
    @Transactional
    fun create(request: UserCreateRequest): UserResponse {
        validateLoginIdNotDuplicate(request.loginId)
        val user = createUser(request)
        createPointAccountFor(user)
        return UserResponse.from(user)
    }

    /**
     * 간편 로그인: 닉네임으로 조회, 없으면 생성 후 반환.
     * @return 로그인(또는 생성)된 User 엔티티
     */
    @Transactional
    fun loginOrCreate(nickname: String): User {
        userRepository.findByLoginId(nickname)?.let { return it }
        val user = createUser(voltup.be.api.dto.request.UserCreateRequest(loginId = nickname, name = nickname))
        createPointAccountFor(user)
        return user
    }

    /**
     * ID로 사용자 조회.
     * @return UserResponse
     * @throws NotFoundException 사용자 없을 때
     */
    @Transactional(readOnly = true)
    fun getById(userId: Long): UserResponse {
        val user = findUserById(userId)
        return UserResponse.from(user)
    }

    /**
     * 내부용: 엔티티 조회. 없으면 예외.
     */
    fun findUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { NotFoundException(ErrorCode.NOT_FOUND_USER) }
    }

    private fun validateLoginIdNotDuplicate(loginId: String) {
        if (userRepository.existsByLoginId(loginId)) {
            throw voltup.be.api.exception.BusinessException(ErrorCode.DUPLICATE_LOGIN_ID)
        }
    }

    private fun createUser(request: UserCreateRequest): User {
        val user = User(loginId = request.loginId, name = request.name)
        return userRepository.save(user)
    }

    private fun createPointAccountFor(user: User) {
        val point = Point(user = user, balance = 0L)
        user.setPointAccount(point)
        pointRepository.save(point)
    }
}
