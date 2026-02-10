package voltup.be.api.exception

import org.springframework.http.HttpStatus

/**
 * API 공통 에러 코드.
 * 목적: 클라이언트가 코드로 분기 처리 가능, 메시지 일원화.
 */
enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // 4xx - 클라이언트 오류
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "C002", "사용자를 찾을 수 없습니다."),
    NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "C003", "상품을 찾을 수 없습니다."),
    NOT_FOUND_POINT(HttpStatus.NOT_FOUND, "C004", "포인트 계정을 찾을 수 없습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "C005", "포인트가 부족합니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "C006", "상품 재고가 부족합니다."),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "C007", "당일 룰렛은 1인 1회만 참여 가능합니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "C008", "이미 사용 중인 로그인 ID입니다."),
    NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "C009", "주문을 찾을 수 없습니다."),
    NOT_FOUND_PARTICIPATION(HttpStatus.NOT_FOUND, "C010", "룰렛 참여 내역을 찾을 수 없습니다."),
    INSUFFICIENT_BUDGET(HttpStatus.BAD_REQUEST, "C011", "일일 예산을 초과하여 지급할 수 없습니다. (꽝)"),
    // INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "C012", "상품 재고가 부족합니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "C013", "이미 취소된 주문입니다."),
    PARTICIPATION_ALREADY_CANCELLED(HttpStatus.CONFLICT, "C014", "이미 취소된 룰렛 참여입니다."),
    INSUFFICIENT_POINT_FOR_RECLAIM(HttpStatus.BAD_REQUEST, "C015", "룰렛 취소 시 회수할 포인트가 부족합니다."),
    BUDGET_CANNOT_DECREASE(HttpStatus.BAD_REQUEST, "C016", "이미 지급되어 더 낮게 수정이 불가합니다."),

    // 5xx - 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 오류가 발생했습니다.")
}
