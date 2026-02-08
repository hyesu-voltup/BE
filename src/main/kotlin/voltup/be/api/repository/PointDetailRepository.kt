package voltup.be.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import voltup.be.api.domain.entity.PointDetail
import voltup.be.api.domain.entity.PointDetailType
import java.time.LocalDateTime

/**
 * 포인트 획득 건별 내역 저장소.
 * 목적: 만료 처리, 7일 이내 만료 예정 조회, 룰렛 취소 시 referenceId로 조회.
 */
interface PointDetailRepository : JpaRepository<PointDetail, Long> {

    /** 만료 미적용 건 중 expiredAt <= 기준 시각 (자정 스케줄용) */
    @Query(
        "SELECT d FROM PointDetail d WHERE d.applied = false AND d.expiredAt <= :before"
    )
    fun findExpiredAndNotApplied(@Param("before") before: LocalDateTime): List<PointDetail>

    /** 7일 이내 만료 예정: applied=false, expiredAt between now and now+7일 (포인트 현황용) */
    @Query(
        "SELECT d FROM PointDetail d WHERE d.point.id = :pointId AND d.applied = false " +
            "AND d.expiredAt > :now AND d.expiredAt <= :end"
    )
    fun findExpiringWithin(
        @Param("pointId") pointId: Long,
        @Param("now") now: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<PointDetail>

    /** 룰렛 취소 시 회수 대상: type=ROULETTE, referenceId=participationId */
    fun findByPointIdAndTypeAndReferenceId(
        pointId: Long,
        type: PointDetailType,
        referenceId: Long
    ): PointDetail?

    /** 해당 포인트 계정의 만료 미적용 건 중 expiredAt <= before (조회 시점 만료 반영용) */
    @Query(
        "SELECT d FROM PointDetail d WHERE d.point.id = :pointId AND d.applied = false AND d.expiredAt <= :before"
    )
    fun findExpiredAndNotAppliedByPointId(
        @Param("pointId") pointId: Long,
        @Param("before") before: LocalDateTime
    ): List<PointDetail>

    /** 유효한(만료되지 않은) 획득 내역: applied=false, expiredAt > now */
    @Query(
        "SELECT d FROM PointDetail d WHERE d.point.id = :pointId AND d.applied = false AND d.expiredAt > :now ORDER BY d.createdAt DESC"
    )
    fun findValidByPointId(
        @Param("pointId") pointId: Long,
        @Param("now") now: LocalDateTime
    ): List<PointDetail>
}
