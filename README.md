# VoltUp API 문서

프론트엔드(Web, App)와의 통신을 위한 REST API 명세입니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [일반 사용자 API](#일반-사용자-api)
3. [관리자(Admin) API](#관리자-admin-api)
4. [에러 응답](#에러-응답)

---

## 공통 사항

### Base URL

- 개발/운영 환경에 따라 `{BASE_URL}/api/v1` 형태로 호출합니다.

### 공통 요청 헤더

| 헤더명        | 필수 | 설명 |
|---------------|------|------|
| `Content-Type` | O    | `application/json` |
| `X-User-Id`   | 조건부 | **로그인 사용자 식별**. 로그인 후 받은 `userId`를 넣어야 하는 API에서 필수 (주문 생성, 포인트 조회, 룰렛 참여 등) |

### 공통 에러 응답 형식

모든 4xx/5xx 응답은 아래 형식으로 통일됩니다.

```json
{
  "code": "C001",
  "message": "잘못된 요청입니다."
}
```

- `code`: 에러 코드 (클라이언트 분기 처리용)
- `message`: 사용자에게 보여줄 수 있는 메시지

에러 코드 목록은 [에러 응답](#에러-응답) 참고.

---

## 일반 사용자 API

### 1. 인증 (Auth)

- **일반 로그인**: 아래와 동일한 `POST /api/v1/auth/login` 사용. 닉네임(로그인 ID) 입력 시 사용자 생성/조회 후 `userId` 반환.
- **어드민 로그인**: 동일한 로그인 API 사용. 단, **로그인 ID가 `ADMIN`으로 시작하는 계정**만 어드민으로 간주됩니다.  
  어드민 전용 API(예산, 주문 목록/취소, 상품 등록/수정, 룰렛 참여 기록/취소)는 **ADMIN으로 시작한 로그인 ID로 로그인한 사용자만** 호출 가능합니다.  
  (일반 로그인과 기능은 같고, 어드민 기능 접근 시 서버에서 로그인 ID prefix로 권한을 구분합니다.)

#### 1.1 간편 로그인

| 항목 | 내용 |
|------|------|
| **역할** | 닉네임(로그인 ID)만으로 로그인. 미가입 시 자동 생성 후 `userId` 반환. 이후 API 호출 시 `X-User-Id`로 사용. 일반/어드민 동일 API. |
| **Method** | `POST` |
| **Path** | `/api/v1/auth/login` |

**Request Body**

```json
{
  "nickname": "홍길동"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| nickname | string | O | 닉네임(또는 아이디). 공백 불가. |

**Response** `200 OK`

```json
{
  "userId": 1,
  "nickname": "홍길동"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | number | 사용자 ID (이후 요청 식별자) |
| nickname | string | 닉네임 |

---

### 2. 사용자 (Users)

#### 2.1 사용자 생성

| 항목 | 내용 |
|------|------|
| **역할** | 로그인 ID·이름으로 사용자 및 포인트 계정 생성 (1인 1계정). |
| **Method** | `POST` |
| **Path** | `/api/v1/users` |

**Request Body**

```json
{
  "loginId": "user01",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| loginId | string | O | 로그인 ID (최대 100자) |
| name | string | O | 이름 (최대 100자) |

**Response** `201 Created`

```json
{
  "id": 1,
  "loginId": "user01",
  "name": "홍길동"
}
```

#### 2.2 사용자 조회

| 항목 | 내용 |
|------|------|
| **역할** | ID로 사용자 정보 조회. |
| **Method** | `GET` |
| **Path** | `/api/v1/users/{userId}` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| userId | number | 사용자 ID |

**Response** `200 OK`

```json
{
  "id": 1,
  "loginId": "user01",
  "name": "홍길동"
}
```

---

### 3. 상품 (Products)

#### 3.1 전체 상품 목록

| 항목 | 내용 |
|------|------|
| **역할** | 모든 사용자가 볼 수 있는 상품 목록 (ID, 상품명, 가격, 재고). |
| **Method** | `GET` |
| **Path** | `/api/v1/products` |

**Response** `200 OK`

```json
[
  {
    "id": 1,
    "name": "기프트카드 5000원",
    "pointPrice": 500,
    "stock": 100
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | number | 상품 ID |
| name | string | 상품명 |
| pointPrice | number | 1개당 포인트 가격 |
| stock | number | 재고 수량 |

- **상품 등록**은 어드민 전용입니다. → [관리자 API > 3. 상품 (Admin Products) > 3.1 상품 등록](#31-상품-등록) 참고.

#### 3.2 상품 조회

| 항목 | 내용 |
|------|------|
| **역할** | ID로 상품 단건 조회. |
| **Method** | `GET` |
| **Path** | `/api/v1/products/{productId}` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| productId | number | 상품 ID |

**Response** `200 OK`

동일한 상품 응답 구조 (id, name, pointPrice, stock).

---

### 4. 주문 (Orders)

#### 4.1 내 주문 내역

| 항목 | 내용 |
|------|------|
| **역할** | 본인의 구매 이력 (품목명, 수량, 사용 포인트, 구매일). |
| **Method** | `GET` |
| **Path** | `/api/v1/orders/me/{userId}` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| userId | number | 사용자 ID |

**Response** `200 OK`

```json
[
  {
    "orderId": 1,
    "productName": "기프트카드 5000원",
    "quantity": 2,
    "usedPoint": 1000,
    "orderedAt": "2025-02-08T14:30:00",
    "status": "ORDERED"
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | number | 주문 ID |
| productName | string | 구매 품목명 |
| quantity | number | 수량 |
| usedPoint | number | 사용 포인트 |
| orderedAt | string | 구매일 (ISO 8601) |
| status | string | ORDERED=승인(기본), CANCELLED=취소(관리자 상품 회수) |

#### 4.2 상품 구매 (주문 생성)

| 항목 | 내용 |
|------|------|
| **역할** | 포인트 차감 및 상품 재고 감소. 유저 포인트·상품 재고에 비관적 락 적용. |
| **Method** | `POST` |
| **Path** | `/api/v1/orders` |

**Request Headers**

| 헤더 | 필수 | 설명 |
|------|------|------|
| X-User-Id | O | 현재 로그인 사용자 ID |

**Request Body**

```json
{
  "productId": 1,
  "quantity": 2
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| productId | number | O | 상품 ID |
| quantity | number | O | 수량 (1 이상) |

**Response** `201 Created`

```json
{
  "orderId": 1,
  "pointAmount": 1000,
  "quantity": 2
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | number | 주문 ID |
| pointAmount | number | 결제 포인트 |
| quantity | number | 주문 수량 |

**에러**

- `400`: 포인트 부족(C005), 재고 부족(C006)
- `404`: 사용자/상품/포인트 없음(C002, C003, C004)

---

### 5. 포인트 (Points)

#### 5.1 내 포인트 현황 (요약)

| 항목 | 내용 |
|------|------|
| **역할** | 가용 잔액 및 7일 이내 만료 예정 포인트. 조회 시점 만료 반영. 포인트 유효기간은 획득일로부터 30일. |
| **Method** | `GET` |
| **Path** | `/api/v1/points/me` |

**Request Headers**

| 헤더 | 필수 | 설명 |
|------|------|------|
| X-User-Id | O | 현재 로그인 사용자 ID |

**Response** `200 OK`

```json
{
  "availableBalance": 1000,
  "expiringWithin7Days": 100
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| availableBalance | number | 가용 잔액 (P) |
| expiringWithin7Days | number | 7일 이내 만료 예정 포인트 (P) |

**에러** `404`: 포인트 계정 없음(C004)

#### 5.2 내 포인트 상세 조회

| 항목 | 내용 |
|------|------|
| **역할** | 총 잔액(만료 반영) + 유효한 포인트 히스토리(내역 설명, 금액, 만료 예정일). |
| **Method** | `GET` |
| **Path** | `/api/v1/points/me/{userId}` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| userId | number | 사용자 ID |

**Response** `200 OK`

```json
{
  "totalBalance": 1000,
  "histories": [
    {
      "description": "룰렛 당첨",
      "amount": 500,
      "expiryDate": "2025-03-10",
      "statusMessage": null
    },
    {
      "description": "룰렛 당첨",
      "amount": 300,
      "expiryDate": null,
      "statusMessage": "관리자에 의해 수거되었습니다"
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| totalBalance | number | 총 잔액 (만료 반영) |
| histories | array | 유효한 획득 내역 + 관리자 수거 내역 리스트 |
| histories[].description | string | 내역 설명 (예: 룰렛 당첨, 주문 취소 환불) |
| histories[].amount | number | 포인트 금액 |
| histories[].expiryDate | string \| null | 만료 예정일 (YYYY-MM-DD). 수거된 건은 null |
| histories[].statusMessage | string \| null | 정상이면 null, 관리자 수거 시 "관리자에 의해 수거되었습니다" |

**에러** `404`: 포인트 계정 없음(C004)

---

### 6. 룰렛 (Roulette)

#### 6.1 룰렛 상태 조회 (일반 사용자 예산 확인)

| 항목 | 내용 |
|------|------|
| **역할** | 당일 룰렛 일일 예산 잔여량(remainingBudget), 당일 참여 여부(alreadyParticipated). 일반 사용자도 남은 예산 확인 가능. |
| **Method** | `GET` |
| **Path** | `/api/v1/roulette/status` |

**Request Headers**

| 헤더 | 필수 | 설명 |
|------|------|------|
| X-User-Id | O | 현재 로그인 사용자 ID |

**Response** `200 OK`

```json
{
  "remainingBudget": 50000,
  "alreadyParticipated": false
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| remainingBudget | number | 오늘 룰렛 일일 예산 잔여 포인트 (P). 0이면 당첨 불가(꽝). |
| alreadyParticipated | boolean | 당일 이미 참여 여부. true면 오늘은 더 이상 참여 불가. |

#### 6.2 룰렛 돌리기 (참여)

| 항목 | 내용 |
|------|------|
| **역할** | 당일 1인 1회 참여. 당첨 시 100P~1000P 랜덤 지급. 일일 예산(10만 P) 내에서만 지급, 초과 시 꽝. 동시 다중 호출 시 1회만 성공, 나머지 409. |
| **Method** | `POST` |
| **Path** | `/api/v1/roulette/participate` |

**Request Headers**

| 헤더 | 필수 | 설명 |
|------|------|------|
| X-User-Id | O | 현재 로그인 사용자 ID |

**Request Body** 없음.

**Response** `200 OK`

```json
{
  "grantedPoint": 500,
  "balanceAfter": 1500,
  "participationId": 1
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| grantedPoint | number | 당첨 지급 포인트 (100~1000P, 꽝이면 0) |
| balanceAfter | number | 참여 후 포인트 잔액 |
| participationId | number | 참여 ID (관리자 취소 시 사용) |

**에러**

- `400`: 일일 예산 초과로 꽝(C011)
- `404`: 사용자/포인트 계정 없음(C002, C004)
- `409`: 당일 이미 참여함(C007)

---

## 관리자 (Admin) API

관리자 전용 기능. **로그인 ID가 `ADMIN`으로 시작하는 계정**으로 로그인한 사용자만 호출 가능합니다. (일반 로그인과 동일한 `POST /api/v1/auth/login` 사용 후, `X-User-Id`로 어드민 API 호출.)

### 1. 예산 (Budget)

#### 1.1 오늘 예산 조회

| 항목 | 내용 |
|------|------|
| **역할** | 당일 누적 지급액 및 잔여 예산(10만 P 한도) 확인. |
| **Method** | `GET` |
| **Path** | `/api/v1/admin/budget` |

**Response** `200 OK`

```json
{
  "budgetDate": "2025-02-08",
  "totalGranted": 50000,
  "remaining": 50000
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| budgetDate | string | 예산 일자 (YYYY-MM-DD) |
| totalGranted | number | 당일 누적 지급 포인트 |
| remaining | number | 잔여 예산 (100,000 - totalGranted) |

#### 1.2 오늘 예산 강제 설정 (잔여 기준)

| 항목 | 내용 |
|------|------|
| **역할** | 당일 **잔여 예산(remaining)** 을 강제 설정. 이미 지급액보다 적게 수정(remaining을 크게)하려 하면 C016. |
| **Method** | `PATCH` |
| **Path** | `/api/v1/admin/budget` |

**Request Body**

```json
{
  "remaining": 50000
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| remaining | number | O | 강제 설정할 당일 잔여 예산 (0 ~ 100,000). 이미 지급된 금액보다 많게 설정 불가 시 C016. |

**Response** `200 OK`

동일한 예산 응답 구조 (budgetDate, totalGranted, remaining).

**에러**

- `400`: 유효하지 않은 값(C001), 이미 지급되어 더 낮게 수정 불가(C016)

---

### 2. 주문 (Admin Orders)

#### 2.1 전체 주문 목록

| 항목 | 내용 |
|------|------|
| **역할** | 서비스 전체 주문 현황 (주문 ID, 유저 ID, 닉네임, 주문 시간, 물품명, 수량). |
| **Method** | `GET` |
| **Path** | `/api/v1/admin/orders` |

**Response** `200 OK`

```json
[
  {
    "orderId": 1,
    "userId": 1,
    "nickname": "홍길동",
    "orderedAt": "2025-02-08T14:30:00",
    "productName": "기프트카드 5000원",
    "quantity": 2
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | number | 주문 ID |
| userId | number | 유저 ID |
| nickname | string | 유저 닉네임 |
| orderedAt | string | 주문 시간 (ISO 8601) |
| productName | string | 물품명 |
| quantity | number | 수량 |

#### 2.2 주문 취소

| 항목 | 내용 |
|------|------|
| **역할** | 주문 상태를 CANCELLED로 변경, 유저 포인트 환불, 상품 재고 복원. |
| **Method** | `POST` |
| **Path** | `/api/v1/admin/orders/{orderId}/cancel` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| orderId | number | 주문 ID |

**Response** `204 No Content` (body 없음)

**에러**

- `404`: 주문 없음(C009)
- `409`: 이미 취소된 주문(C013)

---

### 3. 상품 (Admin Products)

#### 3.1 상품 등록

| 항목 | 내용 |
|------|------|
| **역할** | 상품명·포인트 가격·재고로 상품 생성. **어드민만 호출 가능.** |
| **Method** | `POST` |
| **Path** | `/api/v1/admin/products` |

**Request Body**

```json
{
  "name": "기프트카드 5000원",
  "pointPrice": 500,
  "stock": 100
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | O | 상품명 (최대 200자) |
| pointPrice | number | O | 1개당 포인트 가격 (0 이상) |
| stock | number | - | 재고 (0 이상, 기본 0) |

**Response** `201 Created`

```json
{
  "id": 1,
  "name": "기프트카드 5000원",
  "pointPrice": 500,
  "stock": 100
}
```

**에러** `400`: 유효하지 않은 값(C001)

#### 3.2 상품 삭제

| 항목 | 내용 |
|------|------|
| **역할** | 상품 소프트 삭제. 삭제된 상품은 목록/조회에서 제외됨. |
| **Method** | `DELETE` |
| **Path** | `/api/v1/admin/products/{productId}` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| productId | number | 상품 ID |

**Response** `204 No Content` (body 없음)

**에러** `404`: 상품 없음 또는 이미 삭제됨(C003)

#### 3.3 상품 수정

| 항목 | 내용 |
|------|------|
| **역할** | 상품명, 가격, 재고 수정. 전달한 필드만 변경 (null 필드는 미변경). |
| **Method** | `PUT` |
| **Path** | `/api/v1/admin/products/{productId}` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| productId | number | 상품 ID |

**Request Body**

```json
{
  "name": "기프트카드 10000원",
  "pointPrice": 1000,
  "stock": 50
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | - | 상품명 (보내지 않으면 미변경) |
| pointPrice | number | - | 1개당 포인트 가격 (보내지 않으면 미변경) |
| stock | number | - | 재고 (보내지 않으면 미변경) |

**Response** `200 OK`

일반 상품 응답과 동일 (id, name, pointPrice, stock).

**에러** `404`: 상품 없음(C003)

---

### 4. 룰렛 (Admin Roulette)

#### 4.1 룰렛 참여 기록 목록

| 항목 | 내용 |
|------|------|
| **역할** | 참여 취소(회수)를 위한 목록. participationId, userId, 닉네임, 참여 시간, **지급받은 포인트** 제공. |
| **Method** | `GET` |
| **Path** | `/api/v1/admin/roulette/participations` |

**Response** `200 OK`

```json
[
  {
    "participationId": 1,
    "userId": 1,
    "nickname": "홍길동",
    "participatedAt": "2025-02-08T14:00:00",
    "grantedPoint": 500
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| participationId | number | 참여 ID |
| userId | number | 사용자 ID |
| nickname | string | 유저 닉네임 |
| participatedAt | string | 참여 시간 (ISO 8601) |
| grantedPoint | number | 지급받은 포인트 (P). 꽝이면 0. |

#### 4.2 룰렛 참여 취소

| 항목 | 내용 |
|------|------|
| **역할** | 지급된 포인트 회수 후 참여 취소 처리. 유저 잔액 부족 시 C015. |
| **Method** | `POST` |
| **Path** | `/api/v1/admin/roulette/{participationId}/cancel` |

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| participationId | number | 참여 ID (룰렛 참여 시 반환된 participationId) |

**Response** `204 No Content` (body 없음)

**에러**

- `400`: 포인트 부족으로 회수 불가(C015)
- `404`: 참여 내역 없음(C010)
- `409`: 이미 취소된 참여(C014)

---

## 에러 응답

### HTTP 상태 코드

| 상태 코드 | 의미 |
|-----------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 204 | 성공 (본문 없음) |
| 400 | 잘못된 요청 (비즈니스 규칙 위반 등) |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복 참여, 이미 취소 등) |
| 500 | 서버 오류 |

### 에러 코드 (code)

| 코드 | HTTP | 설명 |
|------|------|------|
| C001 | 400 | 잘못된 요청 |
| C002 | 404 | 사용자를 찾을 수 없음 |
| C003 | 404 | 상품을 찾을 수 없음 |
| C004 | 404 | 포인트 계정을 찾을 수 없음 |
| C005 | 400 | 포인트 부족 |
| C006 | 400 | 상품 재고 부족 |
| C007 | 409 | 당일 룰렛 1인 1회만 참여 가능 |
| C008 | 409 | 이미 사용 중인 로그인 ID |
| C009 | 404 | 주문을 찾을 수 없음 |
| C010 | 404 | 룰렛 참여 내역을 찾을 수 없음 |
| C011 | 400 | 일일 예산 초과 (꽝) |
| C013 | 409 | 이미 취소된 주문 |
| C014 | 409 | 이미 취소된 룰렛 참여 |
| C015 | 400 | 룰렛 취소 시 회수할 포인트 부족 |
| C016 | 400 | 이미 지급되어 더 낮게 수정이 불가합니다. (예산 잔여 설정 시) |
| S001 | 500 | 서버 오류 |

---

## 프론트엔드 연동 요약

| 구분 | 용도 |
|------|------|
| **Web / App 공통** | Base URL + 위 API 경로 사용. 로그인 후 `userId` 저장 후 `X-User-Id` 헤더에 설정. |
| **Request** | JSON body는 문서의 Request Body 참고. `Content-Type: application/json` 필수. |
| **Response** | 성공 시 문서의 Response JSON 파싱. 실패 시 `code`, `message`로 에러 처리 및 사용자 안내. |
| **일반 사용자** | Auth → Users/Products/Orders/Points/Roulette. 주문·포인트·룰렛은 `X-User-Id` 필요. |
| **관리자** | Admin Budget/Orders/Products/Roulette. 로그인 ID가 `ADMIN`으로 시작하는 계정으로 로그인한 후 동일하게 `X-User-Id`로 어드민 API 호출. |

이 문서는 현재 구현된 API 기준으로 작성되었으며, 스펙 변경 시 함께 업데이트해야 합니다.
