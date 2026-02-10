# VoltUp API 문서

전체 API를 문서로 관리하기 위한 명세입니다.  
---

## 목차

1. [개요](#1-개요)
2. [일반 사용자 API 명세서](#2-일반-사용자-api-명세서)
3. [어드민 API 명세서](#3-어드민-api-명세서)
4. [Exception (에러 응답)](#4-exception-에러-응답)
5. [배포 관련](#5-배포-관련)

---

## 1. 개요

### 1.1 문서 목적

- 프론트엔드(Web, App)와의 통신 규격 정의
- Request / Response / 역할 정리로 연동 오류 최소화

### 1.2 Base URL

- 환경에 따라 `{BASE_URL}/api/v1` 형태로 호출
- 예: `https://voltupbe.onrender.com/api/v1`

### 1.3 공통 요청 헤더

| 헤더명         | 필수   | 설명 |
|----------------|--------|------|
| `Content-Type` | O      | `application/json` |
| `X-User-Id`    | 조건부 | 로그인 사용자 식별. 로그인 후 받은 `userId` 사용. 주문 생성, 포인트 조회, 룰렛 참여 등에서 필수 |

### 1.4 인증 구분

- **일반 로그인**: `POST /api/v1/auth/login` (닉네임 입력 → `userId` 반환). 이후 요청에 `X-User-Id` 설정.
- **어드민 로그인**: 동일 로그인 API 사용. **로그인 ID가 `ADMIN`으로 시작하는 계정**만 어드민으로 간주되며, 어드민 전용 API(예산, 주문 목록/취소, 상품 등록·수정·삭제, 룰렛 참여 기록/취소)는 해당 계정으로만 호출 가능.

### 1.5 연동 요약

| 구분           | 용도 |
|----------------|------|
| Web / App 공통 | Base URL + 본 문서 API 경로 사용. 로그인 후 `userId` 저장 → `X-User-Id` 헤더 설정 |
| 일반 사용자    | Auth → Users / Products / Orders / Points / Roulette. 주문·포인트·룰렛은 `X-User-Id` 필요 |
| 관리자         | Admin Budget / Orders / Products / Roulette. `ADMIN` 접두사 계정 로그인 후 동일하게 `X-User-Id`로 호출 |

상세 API는 아래 **일반 사용자 API 명세서**, **어드민 API 명세서** 참고.

---

## 2. 일반 사용자 API 명세서
닉네임을 본인이 원하는 이름 무엇이든 가능합니다. (테스트 계정)

### 2.1 인증 (Auth)

- 일반/어드민 동일: `POST /api/v1/auth/login` 사용.

#### 2.1.1 간편 로그인

| 항목 | 내용 |
|------|------|
| **역할** | 닉네임(로그인 ID)만으로 로그인. 미가입 시 자동 생성 후 `userId` 반환. 이후 `X-User-Id`로 사용. |
| **Method** | `POST` |
| **Path** | `/api/v1/auth/login` |

**Request Body**

```json
{ "nickname": "홍길동" }
```

| 필드     | 타입   | 필수 | 설명 |
|----------|--------|------|------|
| nickname | string | O    | 닉네임(또는 아이디). 공백 불가. |

**Response** `200 OK`

```json
{ "userId": 1, "nickname": "홍길동" }
```

| 필드     | 타입   | 설명 |
|----------|--------|------|
| userId   | number | 이후 요청 식별자 |
| nickname | string | 닉네임 |

---

### 2.2 사용자 (Users)

#### 2.2.1 사용자 생성

| 항목 | 내용 |
|------|------|
| **역할** | 로그인 ID·이름으로 사용자 및 포인트 계정 생성 (1인 1계정). |
| **Method** | `POST` |
| **Path** | `/api/v1/users` |

**Request Body**

```json
{ "loginId": "user01", "name": "홍길동" }
```

| 필드    | 타입   | 필수 | 설명 |
|---------|--------|------|------|
| loginId | string | O    | 로그인 ID (최대 100자) |
| name    | string | O    | 이름 (최대 100자) |

**Response** `201 Created`

```json
{ "id": 1, "loginId": "user01", "name": "홍길동" }
```

#### 2.2.2 사용자 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **Path** | `/api/v1/users/{userId}` |

**Path Parameters**: `userId` (number)

**Response** `200 OK`: 동일 구조 (id, loginId, name)

---

### 2.3 상품 (Products)

- 상품 **등록**은 어드민 전용 → [3.3.1 상품 등록](#331-상품-등록) 참고.

#### 2.3.1 전체 상품 목록

| 항목 | 내용 |
|------|------|
| **역할** | 모든 사용자가 볼 수 있는 상품 목록 (ID, 상품명, 가격, 재고). |
| **Method** | `GET` |
| **Path** | `/api/v1/products` |

**Response** `200 OK`

```json
[
  { "id": 1, "name": "기프트카드 5000원", "pointPrice": 500, "stock": 100 }
]
```

| 필드       | 타입   | 설명 |
|------------|--------|------|
| id         | number | 상품 ID |
| name       | string | 상품명 |
| pointPrice | number | 1개당 포인트 가격 |
| stock      | number | 재고 수량 |

#### 2.3.2 상품 조회

| **Method** | `GET` |
| **Path** | `/api/v1/products/{productId}` |

**Path Parameters**: `productId` (number)  
**Response** `200 OK`: 위와 동일한 상품 객체.

---

### 2.4 주문 (Orders)

#### 2.4.1 내 주문 내역

| 항목 | 내용 |
|------|------|
| **역할** | 본인 구매 이력 (품목명, 수량, 사용 포인트, 구매일, 상태). |
| **Method** | `GET` |
| **Path** | `/api/v1/orders/me/{userId}` |

**Path Parameters**: `userId` (number)

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

| 필드        | 타입   | 설명 |
|-------------|--------|------|
| orderId     | number | 주문 ID |
| productName | string | 구매 품목명 |
| quantity    | number | 수량 |
| usedPoint   | number | 사용 포인트 |
| orderedAt   | string | 구매일 (ISO 8601) |
| status      | string | ORDERED=승인, CANCELLED=취소(관리자 회수) |

#### 2.4.2 상품 구매 (주문 생성)

| 항목 | 내용 |
|------|------|
| **역할** | 포인트 차감 및 상품 재고 감소. 포인트·재고 비관적 락 적용. |
| **Method** | `POST` |
| **Path** | `/api/v1/orders` |

**Request Headers**: `X-User-Id` (O)

**Request Body**

```json
{ "productId": 1, "quantity": 2 }
```

| 필드      | 타입   | 필수 | 설명 |
|-----------|--------|------|------|
| productId | number | O    | 상품 ID |
| quantity  | number | O    | 수량 (1 이상) |

**Response** `201 Created`

```json
{ "orderId": 1, "pointAmount": 1000, "quantity": 2 }
```

**에러**: `400` 포인트 부족(C005), 재고 부족(C006) / `404` 사용자·상품·포인트 없음(C002, C003, C004)

---

### 2.5 포인트 (Points)

#### 2.5.1 내 포인트 현황 (요약)

| 항목 | 내용 |
|------|------|
| **역할** | 가용 잔액, 7일 이내 만료 예정 포인트. 조회 시점 만료 반영. 유효기간 획득일+30일. |
| **Method** | `GET` |
| **Path** | `/api/v1/points/me` |

**Request Headers**: `X-User-Id` (O)

**Response** `200 OK`

```json
{ "availableBalance": 1000, "expiringWithin7Days": 100 }
```

**에러**: `404` 포인트 계정 없음(C004)

#### 2.5.2 내 포인트 상세 조회

| **Method** | `GET` |
| **Path** | `/api/v1/points/me/{userId}` |

**Path Parameters**: `userId` (number)

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

| 필드                    | 타입   | 설명 |
|-------------------------|--------|------|
| totalBalance            | number | 총 잔액 (만료 반영) |
| histories[].description | string | 내역 설명 |
| histories[].amount      | number | 포인트 금액 |
| histories[].expiryDate  | string \| null | 만료 예정일. 수거된 건 null |
| histories[].statusMessage | string \| null | 정상 null, 수거 시 "관리자에 의해 수거되었습니다" |

**에러**: `404` 포인트 계정 없음(C004)

---

### 2.6 룰렛 (Roulette)

#### 2.6.1 룰렛 상태 조회 (일반 사용자 예산 확인)

| 항목 | 내용 |
|------|------|
| **역할** | 당일 룰렛 잔여 예산(remainingBudget), 당일 참여 여부(alreadyParticipated). |
| **Method** | `GET` |
| **Path** | `/api/v1/roulette/status` |

**Request Headers**: `X-User-Id` (O)

**Response** `200 OK`

```json
{ "remainingBudget": 50000, "alreadyParticipated": false }
```

| 필드               | 타입    | 설명 |
|--------------------|---------|------|
| remainingBudget    | number  | 오늘 룰렛 잔여 예산 (P). 0이면 당첨 불가. |
| alreadyParticipated | boolean | 당일 이미 참여 여부 |

#### 2.6.2 룰렛 돌리기 (참여)

| 항목 | 내용 |
|------|------|
| **역할** | 당일 1인 1회 참여. 당첨 시 100P~1000P 랜덤 지급. 일일 예산 10만 P 한도. |
| **Method** | `POST` |
| **Path** | `/api/v1/roulette/participate` |

**Request Headers**: `X-User-Id` (O)  
**Request Body**: 없음

**Response** `200 OK`

```json
{ "grantedPoint": 500, "balanceAfter": 1500, "participationId": 1 }
```

**에러**: `400` 일일 예산 초과 꽝(C011) / `404` 사용자·포인트 없음(C002, C004) / `409` 당일 이미 참여(C007)

---

## 3. 어드민 API 명세서

**전제**: 로그인 ID가 `ADMIN`으로 시작하는 계정으로 로그인한 후, 동일하게 `X-User-Id`로 어드민 API 호출. 현재 `ADMIN` 으로 시작하는 아이디 무엇이든 가능. (테스트 계정)

### 3.1 예산 (Budget)

#### 3.1.1 오늘 예산 조회

| **Method** | `GET` |
| **Path** | `/api/v1/admin/budget` |

**Response** `200 OK`

```json
{ "budgetDate": "2025-02-08", "totalGranted": 50000, "remaining": 50000 }
```

| 필드         | 타입   | 설명 |
|--------------|--------|------|
| budgetDate   | string | 예산 일자 (YYYY-MM-DD) |
| totalGranted | number | 당일 누적 지급 포인트 |
| remaining    | number | 잔여 예산 (100,000 - totalGranted) |

#### 3.1.2 오늘 예산 강제 설정 (잔여 기준)

| 항목 | 내용 |
|------|------|
| **역할** | 당일 **잔여 예산(remaining)** 강제 설정. 이미 지급액보다 적게 수정 불가 시 C016. |
| **Method** | `PATCH` |
| **Path** | `/api/v1/admin/budget` |

**Request Body**

```json
{ "remaining": 50000 }
```

| 필드      | 타입   | 필수 | 설명 |
|-----------|--------|------|------|
| remaining | number | O    | 잔여 예산 (0 ~ 100,000). 이미 지급된 금액보다 많게 설정 불가 시 C016. |

**Response** `200 OK`: 위와 동일한 예산 응답 구조.

**에러**: `400` 유효하지 않은 값(C001), 이미 지급되어 더 낮게 수정 불가(C016)

---

### 3.2 주문 (Admin Orders)

#### 3.2.1 전체 주문 목록

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

#### 3.2.2 주문 취소

| **역할** | 주문 CANCELLED 처리, 포인트 환불, 재고 복원. |
| **Method** | `POST` |
| **Path** | `/api/v1/admin/orders/{orderId}/cancel` |

**Path Parameters**: `orderId` (number)  
**Response** `204 No Content`

**에러**: `404` 주문 없음(C009) / `409` 이미 취소(C013)

---

### 3.3 상품 (Admin Products)

#### 3.3.1 상품 등록

| **역할** | 상품명·포인트 가격·재고로 상품 생성. 어드민 전용. |
| **Method** | `POST` |
| **Path** | `/api/v1/admin/products` |

**Request Body**

```json
{ "name": "기프트카드 5000원", "pointPrice": 500, "stock": 100 }
```

| 필드       | 타입   | 필수 | 설명 |
|------------|--------|------|------|
| name       | string | O    | 상품명 (최대 200자) |
| pointPrice | number | O    | 1개당 포인트 가격 (0 이상) |
| stock      | number | -    | 재고 (0 이상, 기본 0) |

**Response** `201 Created`: 상품 객체 (id, name, pointPrice, stock)

**에러**: `400` 유효하지 않은 값(C001)

#### 3.3.2 상품 삭제

| **역할** | 상품 소프트 삭제. 삭제된 상품은 목록/조회에서 제외. |
| **Method** | `DELETE` |
| **Path** | `/api/v1/admin/products/{productId}` |

**Path Parameters**: `productId` (number)  
**Response** `204 No Content`

**에러**: `404` 상품 없음 또는 이미 삭제(C003)

#### 3.3.3 상품 수정

| **역할** | 상품명, 가격, 재고 수정. 전달한 필드만 변경. |
| **Method** | `PUT` |
| **Path** | `/api/v1/admin/products/{productId}` |

**Path Parameters**: `productId` (number)

**Request Body**

```json
{ "name": "기프트카드 10000원", "pointPrice": 1000, "stock": 50 }
```

| 필드       | 타입   | 필수 | 설명 |
|------------|--------|------|------|
| name       | string | -    | 상품명 (미전달 시 유지) |
| pointPrice | number | -    | 1개당 포인트 가격 (미전달 시 유지) |
| stock      | number | -    | 재고 (미전달 시 유지) |

**Response** `200 OK`: 상품 객체

**에러**: `404` 상품 없음(C003)

---

### 3.4 룰렛 (Admin Roulette)

#### 3.4.1 룰렛 참여 기록 목록

| **역할** | 참여 취소(회수)용 목록. participationId, userId, 닉네임, 참여 시간, 지급 포인트. |
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

#### 3.4.2 룰렛 참여 취소

| **역할** | 지급 포인트 회수 후 참여 취소. 잔액 부족 시 C015. |
| **Method** | `POST` |
| **Path** | `/api/v1/admin/roulette/{participationId}/cancel` |

**Path Parameters**: `participationId` (number)  
**Response** `204 No Content`

**에러**: `400` 포인트 부족 회수 불가(C015) / `404` 참여 내역 없음(C010) / `409` 이미 취소(C014)

---

## 4. Exception (에러 응답)

### 4.1 공통 형식

모든 4xx/5xx 응답은 아래 형식으로 통일됩니다.

```json
{
  "code": "C001",
  "message": "잘못된 요청입니다."
}
```

- `code`: 클라이언트 분기 처리용 에러 코드
- `message`: 사용자 노출용 메시지

### 4.2 HTTP 상태 코드

| 상태 코드 | 의미 |
|-----------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 204 | 성공 (본문 없음) |
| 400 | 잘못된 요청 (비즈니스 규칙 위반 등) |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복 참여, 이미 취소 등) |
| 500 | 서버 오류 |

### 4.3 에러 코드 (code)

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

## 5. 배포 관련

### 5.1 기술 스택

- **언어**: Kotlin
- **프레임워크**: Spring Boot
- **JDK**: Amazon Corretto 17
- **DB**: PostgreSQL (배포 시, Hikari + NEON 등)

### 5.2 빌드 및 실행

- **로컬 빌드**: `./gradlew clean bootJar` (테스트 제외 시 `-x test`)
- **실행**: `java -jar build/libs/*.jar` (기본 포트 8080)
- **배포 프로필**: `-Dspring.profiles.active=deploy` 사용 시 `application-deploy.properties` 적용

### 5.3 Docker

- **Dockerfile**: 2단계 빌드 (빌드 스테이지 → 실행 스테이지)
  - Base: `amazoncorretto:17-alpine`
  - 빌드: `./gradlew clean bootJar -x test`
  - 실행: `deploy` 프로필, 포트 **10000**
- **실행 예**:
  - `docker build -t voltup-api .`
  - `docker run -p 10000:10000 -e DB_URL=... voltup-api`

### 5.4 배포 환경 설정

- **포트**: 배포 시 `10000` (Render 등 호스팅 기본값)
- **프로필**: `deploy`
- **필수 환경 변수**: `DB_URL` (PostgreSQL JDBC URL). 필요 시 `DB_USERNAME`, `DB_PASSWORD` 등 추가
- **JPA**: `spring.jpa.hibernate.ddl-auto=update` (배포 설정 기준)

### 5.5 CI/CD

- **GitHub Actions**: `main`, `feat/v1` 브랜치 push 시 Render 배포 트리거
- **워크플로**: `.github/workflows/cicd.yml` — `RENDER_DEPLOY_URL` 시크릿으로 GET 요청

### 5.6 API 문서 (Swagger)

- **배포 환경**: Swagger UI 고정 URL 예: `https://voltupbe.onrender.com`
- **경로**: `/swagger-ui.html` (springdoc 설정 기준)
- **설정**: `springdoc.api-docs.enabled=true`, `springdoc.swagger-ui.enabled=true`

---