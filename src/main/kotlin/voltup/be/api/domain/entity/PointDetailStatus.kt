package voltup.be.api.domain.entity

/**
 * 포인트 내역 상태.
 * 목적: 히스토리에서 "정상" vs "관리자 수거/거부" 표시.
 */
enum class PointDetailStatus {
    /** 정상 적립 (기본) */
    OK,
    /** 관리자에 의해 수거됨 (룰렛 취소 등) */
    RECLAIMED_BY_ADMIN
}
