package voltup.be.api.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import voltup.be.api.domain.entity.Order

/**
 * Order 엔티티 저장소.
 * 목적: 주문 저장·조회. 취소 시 비관적 락으로 이중 취소 방지.
 */
interface OrderRepository : JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Order?

    /** 주문 목록 조회. user, product JOIN FETCH로 N+1·LazyInitializationException 방지 (배포 환경 500 에러 대응). */
    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(@Param("userId") userId: Long): List<Order>

    /** 전체 주문 목록. user, product JOIN FETCH로 LazyInitializationException 방지. */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product ORDER BY o.createdAt DESC")
    fun findAllByOrderByCreatedAtDesc(): List<Order>
}
