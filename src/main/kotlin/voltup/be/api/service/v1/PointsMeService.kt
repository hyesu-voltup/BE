package voltup.be.api.service.v1

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.PointDetail
import voltup.be.api.domain.entity.PointDetailType
import voltup.be.api.dto.v1.PointHistoryItemResponse
import voltup.be.api.dto.v1.PointsDetailResponse
import voltup.be.api.exception.NotFoundException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.repository.PointDetailRepository
import voltup.be.api.repository.PointRepository
import voltup.be.api.domain.service.PointDomainService
import voltup.be.api.service.UserService
import java.time.LocalDateTime

/**
 * 내 포인트 현황·상세 서비스 (v1).
 * 목적: 가용 잔액, 7일 만료 예정, 상세(총 잔액 + 유효 히스토리). 조회 시점 만료 반영.
 */
@Service
class PointsMeService(
    private val userService: UserService,
    private val pointRepository: PointRepository,
    private val pointDetailRepository: PointDetailRepository,
    private val pointDomainService: PointDomainService
) {

    @Transactional(readOnly = true)
    fun getMyPoints(userId: Long): PointsMeResult {
        userService.findUserById(userId)
        val point = findPointByUserId(userId)
        val now = LocalDateTime.now()
        val end = now.plusDays(7)
        val expiringDetails = pointDetailRepository.findExpiringWithin(point.id!!, now, end)
        val expiringWithin7Days = expiringDetails.sumOf { it.amount }
        return PointsMeResult(
            availableBalance = point.balance,
            expiringWithin7Days = expiringWithin7Days
        )
    }

    /**
     * 내 포인트 상세: 조회 시점 만료 반영 후 총 잔액 + 유효한 획득 내역 리스트.
     */
    @Transactional
    fun getMyPointDetail(userId: Long): PointsDetailResponse {
        userService.findUserById(userId)
        val point = findPointByUserId(userId)
        pointDomainService.expireForPoint(point)
        val totalBalance = point.balance
        val now = LocalDateTime.now()
        val validDetails = pointDetailRepository.findValidByPointId(point.id!!, now)
        val histories = validDetails.map { toHistoryItem(it) }
        return PointsDetailResponse(totalBalance = totalBalance, histories = histories)
    }

    private fun findPointByUserId(userId: Long) =
        pointRepository.findByUserId(userId) ?: throw NotFoundException(ErrorCode.NOT_FOUND_POINT)

    private fun toHistoryItem(d: PointDetail): PointHistoryItemResponse {
        val description = when (d.type) {
            PointDetailType.ROULETTE -> "룰렛 당첨"
            PointDetailType.REFUND -> "주문 취소 환불"
        }
        return PointHistoryItemResponse(
            description = description,
            amount = d.amount,
            expiryDate = d.expiredAt.toLocalDate()
        )
    }

    data class PointsMeResult(
        val availableBalance: Long,
        val expiringWithin7Days: Long
    )
}
