# VoltUp 도메인(엔티티) 정리

API 서비스에서 사용하는 DB 엔티티와 열거형을 정리한 문서입니다.

---

## 공통 기반: BaseEntity

모든 엔티티가 상속하는 추상 클래스.

| 필드 | 타입 | 설명 |
|------|------|------|
| createdAt | LocalDateTime | 생성 시각 (수정 불가) |
| updatedAt | LocalDateTime | 수정 시각. `touch()` 호출 시 갱신 |

- **역할**: 감사(Audit) 및 생성/수정 시각 공통 관리.

---

## 1. User (사용자)

**테이블**: `users`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자 |
| loginId | String(100), unique | 로그인 ID. `ADMIN`으로 시작하면 어드민 계정 |
| name | String(100) | 이름(닉네임) |

- **역할**: 룰렛/상품 구매의 주체. 1인 1포인트 계정(Point)과 1:1 연결.
- **관계**: Point와 OneToOne (mappedBy=user).

---

## 2. Point (포인트 계정)

**테이블**: `points`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자 |
| user | User (FK) | 소유 사용자. 1:1 |
| balance | Long | 현재 잔액 (P). 차감/적립 시 비관적 락으로 정합성 유지 |

- **역할**: 사용자별 포인트 잔액. 적립은 PointDetail 건별로 만료일 관리, 잔액은 여기서만 집계.
- **도메인 메서드**: `deduct(amount)`, `add(amount)`, `hasEnough(amount)`, `deductForExpiry(amount)`.

---

## 3. PointDetail (포인트 건별 내역)

**테이블**: `point_details`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자 |
| point | Point (FK) | 소속 포인트 계정 |
| amount | Long | 해당 건 포인트 금액 |
| expiredAt | LocalDateTime | 만료 시각 (생성일+30일) |
| applied | Boolean | 만료 처리 여부. 자정 스케줄에서 balance 반영 후 true |
| type | PointDetailType | ROULETTE(룰렛 당첨), REFUND(주문 취소 환불) |
| referenceId | Long? | ROULETTE → participationId(DailyBudget.id), REFUND → orderId |
| status | PointDetailStatus | OK(정상), RECLAIMED_BY_ADMIN(관리자 수거). 히스토리 표시용 |

- **역할**: 적립 건별 만료일 관리, 7일 이내 만료 예정 조회, 히스토리 표시(정상/수거).
- **만료**: 매일 자정 또는 조회 시점에 `expiredAt` 지난 건은 balance에서 차감 후 `applied=true`. `status=OK`인 건만 만료 대상.

---

## 4. PointDetailType (포인트 내역 유형)

| 값 | 설명 |
|----|------|
| ROULETTE | 룰렛 당첨 적립. referenceId = DailyBudget.id(참여 ID) |
| REFUND | 주문 취소 환불. referenceId = orderId |

- **역할**: 환불/룰렛 취소 시 해당 타입의 PointDetail을 찾아 처리.

---

## 5. PointDetailStatus (포인트 내역 상태)

| 값 | 설명 |
|----|------|
| OK | 정상 적립 (기본). 히스토리에서 만료일 등 표시 |
| RECLAIMED_BY_ADMIN | 관리자에 의해 수거됨. 히스토리에서 "관리자에 의해 수거되었습니다" 표시 |

- **역할**: 과거 히스토리 유지. 회수 시 삭제하지 않고 status만 변경.

---

## 6. Product (상품)

**테이블**: `products`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자 |
| name | String(200) | 상품명 |
| pointPrice | Long | 1개당 필요 포인트 |
| stock | Int | 재고 수량 (0 이상) |
| deleted | Boolean | 어드민 삭제 여부. true면 목록/조회에서 제외(소프트 삭제) |

- **역할**: 포인트 가격·재고 보유. 주문 시 비관적 락으로 재고 정합성 유지.
- **도메인 메서드**: `deductStock(quantity)`, `addStock(quantity)`, `totalPointFor(quantity)`, `hasStock(quantity)`.

---

## 7. Order (주문)

**테이블**: `orders`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자 |
| user | User (FK) | 주문자 |
| product | Product (FK) | 구매 상품 |
| quantity | Int | 수량 |
| pointAmount | Long | 결제 포인트 |
| status | OrderStatus | ORDERED(승인, 기본), CANCELLED(취소) |

- **역할**: 상품 구매 이력. 어드민이 주문 취소(상품 회수) 시 status → CANCELLED, 포인트 환불·재고 복원.

---

## 8. OrderStatus (주문 상태)

| 값 | 설명 |
|----|------|
| ORDERED | 주문 완료(승인). 기본 상태 |
| CANCELLED | 주문 취소됨. 어드민에 의한 상품 회수 시 사용, 환불 완료 |

---

## 9. SystemDailyBudget (시스템 일일 예산)

**테이블**: `system_daily_budgets`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자 |
| budgetDate | LocalDate, unique | 예산 적용 일자 |
| totalGranted | Long | 당일 누적 지급 포인트 (최대 100,000) |

- **역할**: 룰렛 당일 지급 한도(10만 P). 참여 시 비관적 락으로 (totalGranted + 이번 당첨금) ≤ 100,000 보장.
- **상수**: `DAILY_LIMIT = 100_000`.
- **도메인 메서드**: `addGranted(amount)`, `canGrant(amount)`, `subtractGranted(amount)`.

---

## 10. DailyBudget (유저별 일별 룰렛 참여)

**테이블**: `daily_budgets`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 식별자. 룰렛 참여 ID(participationId)로 사용 |
| user | User (FK) | 참여 사용자 |
| budgetDate | LocalDate | 참여/예산 적용 일자 |
| grantedPoint | Long | 해당 참여에서 지급받은 포인트 |
| cancelled | Boolean | 어드민에 의한 참여 취소 여부 |

- **유니크**: (user_id, budget_date) → 1인 1일 1회 참여 제한.
- **역할**: 룰렛 1인 1회 제한, 참여 취소 시 포인트 회수 및 예산 반환.
- **도메인 메서드**: `addGrantedPoint(amount)`, `cancel()`.

---

## 관계 요약

```
User 1:1 Point
Point 1:N PointDetail (적립 건별)
User 1:N Order
Order N:1 Product
SystemDailyBudget (일자별 1건)
User + budgetDate → DailyBudget (1일 1건)
```

- **일반 조회**: User → Point 잔액, PointDetail로 만료/히스토리.
- **룰렛**: SystemDailyBudget(당일 한도) + DailyBudget(유저당 1회) + Point 적립 + PointDetail(ROULETTE).
- **주문**: Order(ORDERED/CANCELLED) + Point 차감/환불 + PointDetail(REFUND) + Product 재고.

이 문서는 현재 구현된 엔티티 기준이며, 스키마/비즈니스 변경 시 함께 수정해야 합니다.
