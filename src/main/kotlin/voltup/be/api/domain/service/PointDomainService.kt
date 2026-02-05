package voltup.be.api.domain.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.Point
import voltup.be.api.domain.entity.PointDetail
import voltup.be.api.domain.entity.PointDetailType
import voltup.be.api.exception.InsufficientPointForReclaimException
import voltup.be.api.exception.NotFoundException
import voltup.be.api.exception.ErrorCode
import voltup.be.api.repository.PointDetailRepository
import java.time.LocalDateTime

/**
 * 포인트 도메인 서비스 (DDD).
 * 목적: 차감/적립/환불/회수 로직을 한곳에서 관리하여 정합성 보장.
 *
 * 포인트 유효기간: 획득일로부터 30일. 만료된 포인트는 매일 자정 스케줄에서 차감되어 사용 불가 처리됨.
 */
@Service
class PointDomainService(
    private val pointDetailRepository: PointDetailRepository
) {

    /** 포인트 유효기간(일). 획득일+30일 초과 시 만료되어 사용 불가. */
    private val expiryDays = 30L

    /**
     * 포인트 적립 (룰렛 당첨, 환불 등). PointDetail 생성(만료일 = 생성일+30일).
     * @param point 락 걸린 Point 엔티티
     * @param amount 지급 포인트
     * @param type ROULETTE | REFUND
     * @param referenceId ROULETTE → participationId(DailyBudget.id), REFUND → orderId
     */
    @Transactional
    fun add(
        point: Point,
        amount: Long,
        type: PointDetailType,
        referenceId: Long?
    ) {
        require(amount > 0) { "적립 포인트는 0보다 커야 합니다." }
        point.add(amount)
        val expiredAt = LocalDateTime.now().plusDays(expiryDays)
        val detail = PointDetail(
            point = point,
            amount = amount,
            expiredAt = expiredAt,
            applied = false,
            type = type,
            referenceId = referenceId
        )
        pointDetailRepository.save(detail)
    }

    /**
     * 포인트 차감 (상품 구매 등). PointDetail 미생성.
     */
    @Transactional
    fun deduct(point: Point, amount: Long) {
        point.deduct(amount)
    }

    /**
     * 주문 취소 환불. 적립과 동일하게 PointDetail(REFUND) 생성.
     */
    @Transactional
    fun refund(point: Point, amount: Long, orderId: Long) {
        add(point, amount, PointDetailType.REFUND, orderId)
    }

    /**
     * 룰렛 취소 회수. 해당 참여 건 PointDetail 조회 후 잔액 검사·차감·Detail 삭제.
     * @param point 락 걸린 Point
     * @param participationId DailyBudget.id
     * @throws NotFoundException 해당 참여 건 PointDetail 없을 때
     * @throws InsufficientPointForReclaimException 잔액 부족 시
     */
    @Transactional
    fun reclaim(point: Point, participationId: Long) {
        val detail = pointDetailRepository.findByPointIdAndTypeAndReferenceId(
            point.id!!,
            PointDetailType.ROULETTE,
            participationId
        ) ?: throw NotFoundException(ErrorCode.NOT_FOUND_PARTICIPATION)
        val reclaimAmount = detail.amount
        if (point.balance < reclaimAmount) {
            throw InsufficientPointForReclaimException(point.balance, reclaimAmount)
        }
        point.deduct(reclaimAmount)
        pointDetailRepository.delete(detail)
    }
}
