package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 포인트 획득 건별 내역 (만료일 관리).
 * 목적: 생성일+30일 만료, 매일 자정 만료 처리, 7일 이내 만료 예정 조회.
 */
@Entity
@Table(name = "point_details")
class PointDetail(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    var point: Point,

    @Column(nullable = false)
    var amount: Long,

    /** 만료 시각 (생성일 + 30일). */
    @Column(name = "expired_at", nullable = false)
    var expiredAt: LocalDateTime,

    /** 만료 처리 여부 (자정 스케줄에서 balance 반영 후 true) */
    @Column(nullable = false)
    var applied: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: PointDetailType,

    /** ROULETTE → participationId(DailyBudget.id), REFUND → orderId */
    @Column(name = "reference_id")
    var referenceId: Long? = null,

    /** 내역 상태. OK=정상, RECLAIMED_BY_ADMIN=관리자 수거(히스토리에 "관리자에 의해 수거되었습니다" 표시) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: PointDetailStatus = PointDetailStatus.OK
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
