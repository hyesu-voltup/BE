package voltup.be.api.domain.entity

/**
 * 포인트 적립 유형 (만료일 계산·회수 시 참조용).
 * 목적: 환불/룰렛 취소 시 해당 타입의 PointDetail을 찾아 처리.
 */
enum class PointDetailType {
    /** 룰렛 당첨 */
    ROULETTE,
    /** 주문 취소 환불 */
    REFUND
}
