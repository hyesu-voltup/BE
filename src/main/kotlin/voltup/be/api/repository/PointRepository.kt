package voltup.be.api.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import voltup.be.api.domain.entity.Point

/**
 * Point 엔티티 저장소.
 * 목적: 포인트 잔액 조회/저장. 차감·적립 시 비관적 락으로 동시성 제어.
 */
interface PointRepository : JpaRepository<Point, Long> {

    fun findByUserId(userId: Long): Point?

    /**
     * 비관적 락으로 포인트 조회 (룰렛 참여/상품 구매 시 사용).
     * @return 락이 걸린 Point 또는 null
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId")
    fun findByUserIdForUpdate(@Param("userId") userId: Long): Point?
}
