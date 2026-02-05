package voltup.be.api.exception

/**
 * 상품 재고 부족 시.
 * 목적: 주문 시 재고 부족 클라이언트 메시지 일원화.
 */
class InsufficientStockException(
    val currentStock: Int,
    val requestedQuantity: Int,
    message: String? = "재고 부족: 현재=$currentStock, 요청=$requestedQuantity"
) : BusinessException(ErrorCode.INSUFFICIENT_STOCK, message)
