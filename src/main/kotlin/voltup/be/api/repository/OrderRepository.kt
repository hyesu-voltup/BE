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

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>

    fun findAllByOrderByCreatedAtDesc(): List<Order>
}
