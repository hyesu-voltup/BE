package voltup.be.api.service.v1

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.exception.NotFoundException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.repository.PointDetailRepository
import voltup.be.api.repository.PointRepository
import voltup.be.api.service.UserService
import java.time.LocalDateTime

/**
 * 내 포인트 현황 서비스 (v1).
 * 목적: 가용 잔액 및 7일 이내 만료 예정 포인트 조회.
 */
@Service
class PointsMeService(
    private val userService: UserService,
    private val pointRepository: PointRepository,
    private val pointDetailRepository: PointDetailRepository
) {

    @Transactional(readOnly = true)
    fun getMyPoints(userId: Long): PointsMeResult {
        userService.findUserById(userId)
        val point = pointRepository.findByUserId(userId)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_POINT)
        val now = LocalDateTime.now()
        val end = now.plusDays(7)
        val expiringDetails = pointDetailRepository.findExpiringWithin(point.id!!, now, end)
        val expiringWithin7Days = expiringDetails.sumOf { it.amount }
        return PointsMeResult(
            availableBalance = point.balance,
            expiringWithin7Days = expiringWithin7Days
        )
    }

    data class PointsMeResult(
        val availableBalance: Long,
        val expiringWithin7Days: Long
    )
}
