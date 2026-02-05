package voltup.be.api.service.v1

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.DailyBudget
import voltup.be.api.domain.entity.SystemDailyBudget
import voltup.be.api.domain.service.PointDomainService
import voltup.be.api.exception.BusinessException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.exception.InsufficientBudgetException
import voltup.be.api.repository.DailyBudgetRepository
import voltup.be.api.repository.PointRepository
import voltup.be.api.repository.SystemDailyBudgetRepository
import voltup.be.api.service.UserService
import java.time.LocalDate

/**
 * 룰렛 참여 서비스 (v1).
 * 목적: SystemDailyBudget·DailyBudget(1인1회) 정합성 보장, 당첨 시 예산 차감 및 포인트 지급.
 *
 * - 랜덤 포인트: 100p ~ 1000p 범위에서 1회 당첨금 지급.
 * - 같은 유저가 동시에 여러 번 돌려도 DB 복합 유니크(user_id, budget_date)로 1회만 성공, 나머지는 ALREADY_PARTICIPATED.
 * - 예산 정합성: SystemDailyBudget 비관적 락으로 (오늘 총 지급 + 이번 당첨금) <= 10만 P 보장.
 *   예) 잔여 1,000p일 때 5명이 동시에 500p 당첨 시도 → 락으로 선착 2명만 지급, 나머지는 예산 초과(꽝).
 */
@Service
class RouletteParticipateService(
    private val userService: UserService,
    private val systemDailyBudgetRepository: SystemDailyBudgetRepository,
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val pointRepository: PointRepository,
    private val pointDomainService: PointDomainService
) {

    @Transactional
    fun participate(userId: Long): RouletteParticipateResult {
        userService.findUserById(userId)
        val today = LocalDate.now()
        val systemBudget = getOrCreateSystemBudgetWithLock(today)
        val participation = createParticipationOrThrow(userId, today)
        val amount = drawAmount()
        if (!systemBudget.canGrant(amount)) {
            throw InsufficientBudgetException(
                systemBudget.totalGranted,
                amount,
                SystemDailyBudget.DAILY_LIMIT
            )
        }
        systemBudget.addGranted(amount)
        val point = pointRepository.findByUserIdForUpdate(userId)
            ?: throw voltup.be.api.exception.NotFoundException(ErrorCode.NOT_FOUND_POINT)
        pointDomainService.add(point, amount, voltup.be.api.domain.entity.PointDetailType.ROULETTE, participation.id)
        participation.addGrantedPoint(amount)
        return RouletteParticipateResult(
            grantedPoint = amount,
            balanceAfter = point.balance,
            participationId = participation.id!!
        )
    }

    private fun getOrCreateSystemBudgetWithLock(today: LocalDate): SystemDailyBudget {
        systemDailyBudgetRepository.findByBudgetDateForUpdate(today)?.let { return it }
        try {
            systemDailyBudgetRepository.saveAndFlush(SystemDailyBudget(budgetDate = today, totalGranted = 0L))
        } catch (_: DataIntegrityViolationException) {
            // 동시에 다른 트랜잭션이 생성한 경우
        }
        return systemDailyBudgetRepository.findByBudgetDateForUpdate(today)!!
    }

    private fun createParticipationOrThrow(userId: Long, today: LocalDate): DailyBudget {
        val user = userService.findUserById(userId)
        val budget = DailyBudget(user = user, budgetDate = today, grantedPoint = 0L)
        return try {
            dailyBudgetRepository.saveAndFlush(budget)
        } catch (e: DataIntegrityViolationException) {
            throw BusinessException(ErrorCode.ALREADY_PARTICIPATED)
        }
    }

    private fun drawAmount(): Long = (MIN_GRANT..MAX_GRANT).random().toLong()

    data class RouletteParticipateResult(
        val grantedPoint: Long,
        val balanceAfter: Long,
        val participationId: Long
    )

    companion object {
        /** 룰렛 1회 당첨 최소 포인트 (P) */
        private const val MIN_GRANT = 100
        /** 룰렛 1회 당첨 최대 포인트 (P) */
        private const val MAX_GRANT = 1000
    }
}
