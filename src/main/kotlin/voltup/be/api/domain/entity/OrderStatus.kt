package voltup.be.api.domain.entity

/**
 * 주문 상태.
 * 목적: 주문 취소 시 상태 변경 및 환불 로직 분기.
 */
enum class OrderStatus {
    /** 주문 완료 (정상) */
    ORDERED,
    /** 주문 취소됨 (환불 완료) */
    CANCELLED
}
