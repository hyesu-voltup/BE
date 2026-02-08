package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.dto.admin.AdminRouletteParticipationResponse
import voltup.be.api.repository.DailyBudgetRepository

/**
 * 어드민 룰렛 참여 기록 조회 서비스.
 */
@Service
class AdminRouletteQueryService(
    private val dailyBudgetRepository: DailyBudgetRepository
) {

    @Transactional(readOnly = true)
    fun getParticipations(): List<AdminRouletteParticipationResponse> =
        dailyBudgetRepository.findAllByOrderByCreatedAtDesc().map { d ->
            AdminRouletteParticipationResponse(
                participationId = d.id!!,
                userId = d.user.id!!,
                nickname = d.user.name,
                participatedAt = d.createdAt,
                grantedPoint = d.grantedPoint
            )
        }
}
