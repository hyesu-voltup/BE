package voltup.be.api.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.repository.PointDetailRepository
import java.time.LocalDateTime

/**
 * 포인트 만료 스케줄.
 * 목적: 매일 자정에 expiredAt(획득일+30일)이 지난 포인트를 balance에서 차감 → 만료 포인트 사용 불가.
 */
@Component
class PointExpiryScheduler(
    private val pointDetailRepository: PointDetailRepository
) {

    /**
     * 매일 00:00:10 (KST) 실행. 당일 00:00 이전 만료 건 처리.
     */
    @Scheduled(cron = "10 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    fun expirePoints() {
        val before = LocalDateTime.now()
        val list = pointDetailRepository.findExpiredAndNotApplied(before)
        for (detail in list) {
            detail.point.deductForExpiry(detail.amount)
            detail.applied = true
        }
    }
}
