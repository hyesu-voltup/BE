package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.service.PointDomainService
import voltup.be.api.exception.NotFoundException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.repository.DailyBudgetRepository
import voltup.be.api.repository.PointRepository
import voltup.be.api.repository.SystemDailyBudgetRepository

/**
 * 어드민 룰렛 취소 서비스.
 * 목적: 지급 포인트 회수, 참여 취소 처리, 일일 예산 반환. 포인트 부족 시 예외.
 */
@Service
class AdminRouletteCancelService(
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val systemDailyBudgetRepository: SystemDailyBudgetRepository,
    private val pointRepository: PointRepository,
    private val pointDomainService: PointDomainService
) {

    @Transactional
    fun cancelParticipation(participationId: Long) {
        val participation = dailyBudgetRepository.findByIdForUpdate(participationId)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_PARTICIPATION)
        participation.cancel()
        val systemBudget = systemDailyBudgetRepository.findByBudgetDateForUpdate(participation.budgetDate)
        if (systemBudget != null) {
            systemBudget.subtractGranted(participation.grantedPoint)
        }
        val point = pointRepository.findByUserIdForUpdate(participation.user.id!!)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_POINT)
        pointDomainService.reclaim(point, participationId)
    }
}
