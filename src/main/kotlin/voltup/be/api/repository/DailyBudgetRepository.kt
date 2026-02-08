package voltup.be.api.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import voltup.be.api.domain.entity.DailyBudget
import java.time.LocalDate

/**
 * DailyBudget 엔티티 저장소.
 * 목적: 1인 1회 참여는 DB 복합 유니크(user_id, budget_date)로 보장.
 */
interface DailyBudgetRepository : JpaRepository<DailyBudget, Long> {

    fun findByUserIdAndBudgetDate(userId: Long, budgetDate: LocalDate): DailyBudget?
    fun existsByUserIdAndBudgetDate(userId: Long, budgetDate: LocalDate): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyBudget d WHERE d.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): DailyBudget?

    fun findAllByOrderByCreatedAtDesc(): List<DailyBudget>
}
