package voltup.be.api.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import voltup.be.api.domain.entity.SystemDailyBudget
import java.time.LocalDate

/**
 * 시스템 일일 예산 저장소.
 * 목적: 룰렛 참여 시 비관적 락으로 당일 지급 한도 정합성 보장.
 */
interface SystemDailyBudgetRepository : JpaRepository<SystemDailyBudget, Long> {

    fun findByBudgetDate(budgetDate: LocalDate): SystemDailyBudget?

    /**
     * 비관적 락으로 당일 예산 조회 (동시 참여 시 한 건만 예산 반영).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SystemDailyBudget s WHERE s.budgetDate = :date")
    fun findByBudgetDateForUpdate(@Param("date") budgetDate: LocalDate): SystemDailyBudget?
}
