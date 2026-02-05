package voltup.be.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import voltup.be.api.domain.entity.User

/**
 * User 엔티티 저장소.
 * 목적: 사용자 조회/저장. 반환: Optional 또는 Entity.
 */
interface UserRepository : JpaRepository<User, Long> {

    fun findByLoginId(loginId: String): User?
    fun existsByLoginId(loginId: String): Boolean
}
