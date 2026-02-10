# 개발 과정 AI 사용 부분 정리리

---

## 1. 설계 관점

### 1.1 API 명세서 작성

**질문**  
> 현재 구현된 API를 README에 정리해 주세요. 각 API마다 프론트엔드(Web, App)와의 통신을 위한 Request, Response, 역할을 포함해 주세요.

**의도**  
프론트엔드와 백엔드가 동일한 계약을 기준으로 개발할 수 있도록, API를 단일 문서로 정의하고 싶었습니다. 단순 나열이 아니라 엔드포인트별 Request/Response 스키마와 역할이 명시되어, 이후 스펙 불일치와 재작업을 줄이고자 했습니다.

**고민과 해결**  
컨트롤러와 DTO를 전수 조회한 뒤, 실제 Path, Method, Body, 응답 필드를 문서에 맞춰 정리했습니다. 공통 사항(Base URL, 헤더, 에러 형식)을 앞에 두고, 일반 사용자 API와 어드민 API를 구분해 역할이 드러나도록 구성했습니다. 에러 코드는 표로 정리해 클라이언트에서 코드 기준으로 분기 처리할 수 있도록 했습니다.

**생산성 향상**  
한 문서만 맞추면 프론트와 백이 동시에 개발할 수 있었고, “이 API는 이렇게 쓴다”는 논의가 줄어 커뮤니케이션 비용이 감소했습니다.


---

### 1.2 예산·룰렛·주문·포인트 보강 및 도메인 문서화

**질문**  
> 예산 확인은 일반 유저도 볼 수 있게 해 주세요. 예산 강제 설정은 remaining 기준으로 하되, 이미 지급된 금액보다 낮게 설정할 수 없게 해 주세요. 상품 삭제 API를 추가하고, 룰렛 상태 확인 API, 주문·포인트 이력의 status(승인/취소, 정상/수거)를 반영해 주세요. 엔티티 구조는 DOMAIN.md에 정리해 주세요.

**의도**  
“누가 무엇을 볼 수 있는지”, “데이터가 어떻게 변하는지”를 역할·상태·이력 관점에서 정리하고 싶었습니다. 예산은 읽기만 일반 공개, 쓰기는 어드민만 두었고, 주문·포인트는 상태와 이력을 남겨 감사(audit)와 UX를 동시에 만족시키고자 했습니다.

**고민과 해결**  
예산은 일반 유저용 “룰렛 상태” API에 잔여 예산(remainingBudget)을 포함해 별도 예산 API를 늘리지 않고 일관된 진입점을 제공했습니다. 예산 강제 설정은 remaining을 수정하게 하고, “이미 지급된 금액보다 적게 수정 불가”를 비즈니스 규칙(C016)으로 두었습니다. 상품 삭제는 기존 주문 FK 이슈를 피하기 위해 소프트 삭제(deleted 플래그)로 설계했습니다. 주문·포인트 이력에는 status를 두어 승인/취소, 정상/수거를 구분했고, DOMAIN.md에 엔티티·테이블·필드·관계·역할을 정리했습니다.

**생산성 향상**  
역할별 API와 상태 전이가 문서·코드에서 일치해, “이 API는 누가 쓰는지”, “이 상태 다음에 무엇이 올 수 있는지”를 빠르게 파악할 수 있게 되었습니다.


---

### 1.5 포인트 룰렛 API 도메인·계층 설계

**질문**  
> Spring Boot 3.x, Kotlin, JPA, MySQL, Swagger로 포인트 룰렛 API를 개발해 주세요. 예외는 ErrorCode와 @RestControllerAdvice로 처리하고, 동시성은 비관적 락과 복합 유니크(1인 1회)로 보장해 주세요. 클린 코드·DDD·생성자 주입·Swagger @Operation을 지키고, User·Point·Product·Order·DailyBudget 엔티티부터 Controller·Service 순으로 구현해 주세요.

**의도**  
과제 수준의 제약을 명시한 뒤 도메인 모델과 API 계층을 한 번에 설계하고 싶었습니다. 기술 스택뿐 아니라 예외·동시성·코드 스타일까지 넣어 구현이 요구사항을 벗어나지 않게 하고자 했습니다.

**고민과 해결**  
엔티티를 먼저 정하고, BaseEntity(createdAt·updatedAt), 복합 유니크(DailyBudget의 user_id·budget_date), 도메인 로직(Point.deduct/add, Product.deductStock)을 엔티티에 두어 “뚱뚱한 도메인”으로 설계했습니다. Repository에는 비관적 락용 `findByUserIdForUpdate`, `findByIdForUpdate`를 두고, 룰렛 참여·주문 시 해당 메서드만 사용하도록 했습니다. Service는 한 메서드가 한 가지 일만 하도록 쪼개고 생성자 주입만 사용했으며, ErrorCode·BusinessException·GlobalExceptionHandler로 예외를 일원화하고 Swagger에는 @Operation·@ApiResponses를 붙였습니다.

**생산성 향상**  
요구사항이 프롬프트에 명확히 들어가 있어 재확인 없이 구현 방향이 잡혔고, 엔티티 → Repository → Service → Controller 순서로 의존성 방향이 일정하게 유지되었습니다.

---

### 1.6 유저·어드민 API 확장 및 정합성 요구사항

**질문**  
> 유저 API(룰렛 참여, 내 포인트, 주문)와 어드민 API(예산 조회·설정, 주문 취소, 룰렛 참여 취소)를 포함한 전체 API를 구현해 주세요. SystemDailyBudget·DailyBudget 모두 비관적 락을 적용하고, 10만 P 초과·중복 참여를 막아 주세요. PointDetail로 건별 만료(30일)·자정 스케줄 만료, 예산 소진 시 꽝/예외 처리, PointDomainService로 포인트 로직을 일원화해 주세요. 커스텀 예외와 Swagger 상세 적용도 부탁드립니다.

**의도**  
동시성 정합성과 어드민·유저 로직 일치를 최우선으로 두고, 전체 플로우를 한 번에 설계하고 싶었습니다. 예산 한도·1인 1회·만료·회수 같은 규칙이 서비스·도메인 한곳에서만 관리되게 하고자 했습니다.

**고민과 해결**  
SystemDailyBudget(일일 10만 P 한도)와 DailyBudget(유저별 일별 참여)를 분리하고, 룰렛 참여 시 두 엔티티 모두 락을 걸어 예산 초과와 중복 참여를 DB 수준에서 막았습니다. PointDetail(amount, expiredAt, applied, type, referenceId)을 두고 적립 시마다 생성·만료 스케줄에서 applied 처리·잔액 차감으로 “만료 포인트 사용 불가”를 구현했습니다. PointDomainService에서 add·deduct·refund·reclaim만 담당하게 해 주문·룰렛·취소가 동일한 규칙으로 포인트를 다루도록 했고, InsufficientBudgetException·InsufficientStockException 등으로 예외를 세분화해 GlobalExceptionHandler와 Swagger 응답 설명을 맞췄습니다.

**생산성 향상**  
포인트·예산 규칙이 PointDomainService와 엔티티에만 있어 “주문 취소 시 포인트는?” 같은 질문에 한 곳에서 답할 수 있게 되었고, 어드민·유저가 같은 도메인 로직을 사용해 버그 재현·테스트가 단순해졌습니다.

---

## 2. 문제 고민과 해결

### 2.1 예산 강제 설정: totalGranted vs remaining

**질문**  
> 남은 예산 강제 설정 시 totalGranted가 아니라 remaining을 수정하는 API로 해 주세요. totalGranted보다 낮게 설정하려 하면 “이미 지급되어 더 낮게 수정할 수 없습니다” 에러를 반환하고, 그보다 크게 설정하는 것만 허용해 주세요.

**의도**  
운영자가 “지금 남은 예산을 이만큼으로 맞추고 싶다”는 의도(remaining)를 API에 반영하고, 이미 지급한 금액보다 줄이는 설정은 불가능하게 해 데이터 정합성을 지키고 싶었습니다.

**고민과 해결**  
Request를 totalGranted 대신 remaining만 받도록 바꾸고, 서버 내부에서 `newTotalGranted = DAILY_LIMIT - remaining`으로 계산했습니다. `newTotalGranted < currentTotalGranted`이면 “이미 지급되어 더 낮게 수정 불가”로 판단해 C016 비즈니스 예외를 던지도록 했습니다. README와 에러 코드 표에 C016과 메시지를 명시해 클라이언트·운영자가 거부 사유를 바로 이해할 수 있게 했습니다.

**생산성 향상**  
API 의미가 “잔여 설정”으로 통일되어, 운영자 실수로 이미 지급분을 깎는 설정이 서버에서 차단되었고, 에러 메시지로 원인 파악이 빨라졌습니다.

---

### 2.2 포인트 회수 시 이력 유지

**질문**  
> 어드민이 포인트를 회수할 경우 과거 history가 남아야 합니다. status는 기본 ok이고, 취소·회수 시에만 “관리자에 의해 수거되었습니다” 또는 “거부되었습니다”와 같은 메시지를 보여 주세요.

**의도**  
회수한 포인트 건을 삭제하지 않고 이력으로 남기면서, 사용자 화면에서는 상태를 구분해 보여주고 싶었습니다. 삭제 시 감사(audit)와 분쟁 대응이 어렵다고 판단했습니다.

**고민과 해결**  
PointDetail에 status 컬럼을 추가하고 OK / RECLAIMED_BY_ADMIN 두 가지만 사용하도록 했습니다. 회수 시 해당 Row를 delete하지 않고 status만 RECLAIMED_BY_ADMIN으로 바꾸고 잔액만 차감했습니다. 포인트 상세·히스토리 조회 시 statusMessage를 두어 RECLAIMED_BY_ADMIN이면 “관리자에 의해 수거되었습니다”를 내려주도록 했고, 만료·유효 잔액 계산 시에는 status = OK인 건만 포함하도록 쿼리를 제한했습니다.

**생산성 향상**  
회수 이력이 DB에 그대로 남아 운영·CS 대응이 수월해졌고, 클라이언트는 statusMessage만으로 UX 메시지를 통일할 수 있게 되었습니다.

---

### 2.3기존 주문·재고 정합성

**질문**  
> 상품 수정은 있으나 삭제 API가 없습니다. 어드민용 상품 삭제 API를 추가해 주세요. 또한 모든 삭제는 soft deleted되어야 합니다.

**의도**  
어드민이 상품을 “없는 것처럼” 만들고 싶지만, 이미 주문된 건은 그대로 두고 재고·노출만 제어하고 싶었습니다. 물리 삭제 시 FK·재고 이력 문제를 피하고자 했습니다.

**고민과 해결**  
Order가 Product를 FK로 참조하므로 물리 삭제는 제약이 많아, deleted 플래그 방식(소프트 삭제)으로 결정했습니다. Product에 deleted: Boolean을 추가하고, 목록·단건 조회·주문 가능 상품 조회는 모두 deleted = false만 보이도록 Repository·Service를 수정했습니다. 주문 시 사용하는 조회를 삭제된 상품 제외 버전으로 바꿔 삭제된 상품에는 주문이 걸리지 않게 했고, 어드민 삭제 API는 내부에서 deleted = true 업데이트만 수행하도록 했습니다.

**생산성 향상**  
기존 주문·통계는 그대로 두면서 “판매 중단”만 표현할 수 있어, 데이터 정합성과 운영 요구를 동시에 만족시켰습니다.

---

### 2.4 Controller·Service 이름 중복으로 인한 빈·Swagger 충돌

**질문**  
> 새로 만든 Service·Controller 이름이 기존과 같아 문제가 됩니다. 불필요한 것은 제거하고, 필요하면 이름을 수정한 뒤 참조까지 맞춰 주세요.

**의도**  
v1·어드민용으로 추가한 RouletteController, OrderController 등이 기존 controller 패키지의 동일 이름 클래스와 공존하면서 Spring 빈 이름·Swagger 태그가 겹치는 문제를 해소하고 싶었습니다. “어떤 API를 쓰는지”가 코드와 문서에서 명확히 보이게 하고자 했습니다.

**고민과 해결**  
v1·어드민으로 대체된 구 API를 정리했습니다. PointController, OrderController, RouletteController(구)와 PointService·OrderService·RouletteService(구)를 삭제했습니다. UserController·ProductController는 유지하되 경로만 `/api/v1/users`, `/api/v1/products`로 통일해 v1 단일 진입점이 되도록 했습니다. 실제 사용하는 API는 controller.v1·controller.admin과 service.v1·service.admin만 남겨, 동일 클래스명이라도 패키지로 구분되도록 했습니다.

**생산성 향상**  
빌드·실행 시 등록된 Controller를 헷갈리지 않게 했고, Swagger에서도 v1·admin 구분이 명확해져 API 탐색이 빨라졌습니다.

---

### 2.5 룰렛·포인트 규칙 명세

**질문**  
> 랜덤 포인트는 100 P~1,000 P, 포인트 유효기간은 획득일로부터 30일(만료 시 사용 불가), 같은 유저가 동시에 룰렛을 돌려도 한 번만 성공하도록 해 주세요.(비관적 락을 사용합니다.) 예산 1,000 P 남았을 때 5명이 동시에 500 P 당첨을 시도하면 예산 범위 내에서만 지급되도록 해 주세요.

**의도**  
요구사항을 숫자·동작으로 고정해 “몇 P 구간인지”, “만료는 언제인지”, “동시 요청 시 어떻게 되는지”가 코드·문서에서 일치하도록 하고 싶었습니다. 예산 1,000 P일 때 500 P×5명이 동시에 들어오면 2명만 지급·나머지는 꽝이 되도록 정확히 동작해야 했습니다.

**고민과 해결**  
RouletteParticipateService의 MIN_GRANT·MAX_GRANT를 100·1,000으로 변경하고 주석·Swagger에 “100 P~1,000 P 랜덤”을 명시했습니다. PointDomainService·PointExpiryScheduler에는 “획득일+30일, 만료 시 사용 불가”를 주석으로 남기고 만료 처리 로직을 유지했습니다. “같은 유저 동시 1회”는 DailyBudget insert와 (user_id, budget_date) 유니크로 이미 보장되므로 주석으로 명시했고, “예산 범위 내만 지급”은 SystemDailyBudget에 비관적 락을 걸고 canGrant(amount)·addGranted(amount) 순서로 호출해 (총 지급 + 당첨금) ≤ 10만이 되도록 했습니다.

**생산성 향상**  
규칙이 상수·주석·Swagger에 고정되어 “1회당 최대 몇 P인지” 등을 코드만 보고 답할 수 있게 되었고, 예산 시나리오도 “락 + canGrant 체크” 한 조합으로 일관되게 동작하게 되었습니다.

---

### 2.6 설계안과 실제 코드 반영 여부 확인

**질문**  
> 앞에서 말한 내용이 코드에 제대로 반영되었는지 확인해 주세요. 반영되지 않았다면 코드를 작성하지 말고 주석을 통해 알 수 있게 해주세요.

**의도**  
긴 요구사항을 준 뒤 “설계만 설명”받은 상황에서, 실제로 파일이 수정·추가되었는지 검증하고 싶었습니다. “이론상 이렇게 하면 된다”가 아니라 Controller·Service·Repository·DTO가 실제로 반영되어 있는지 확인하고자 했습니다.

**고민과 해결**  
사용자/어드민을 위해 추가적인 API가 필요함을 주석을 통해 확인하였고 이를 통해 구현 전 설계가 먼저 필요한 부분을 적용했습니다.


---

## 3. 생산성 향상 관점

### 3.1 단일 API 명세서로 프론트·백 계약 고정

**질문**  
> 현재 API를 README에 정리해 주세요. 각 API는 프론트(Web, App)와의 통신을 위해 request, response, 역할을 포함해 주세요.

**의도**  
여러 클라이언트(Web, App)가 동일한 백엔드를 바라볼 때, 한 문서를 계약(contract)으로 두어 스펙 불일치와 재작업을 줄이고 싶었습니다.

**고민과 해결**  
컨트롤러·DTO를 기준으로 Path, Method, Request Body/Header/Path Param, Response 필드, 역할, 예시 에러를 표와 JSON 예시로 정리했습니다. “역할”을 각 API마다 한 줄로 적어 “이 API를 왜 쓰는지”를 문서만 보고 이해할 수 있게 했습니다.

**생산성 향상**  
프론트·백이 같은 문서를 기준으로 개발하게 되어 “이 필드 없었나?”와 같은 논의가 줄어들고 병렬 개발 속도가 올라갔습니다.

---

### 3.2 API 경로·패키지 통일로 역할 구분 명확화

**질문**  
> Controller·Service 이름 충돌을 정리할 때, 경로와 패키지도 일반/어드민이 구분되게 해 주세요.

**의도**  
“일반 사용자용”과 “어드민용”을 경로·패키지·Swagger 태그만 봐도 구분하고 싶었습니다. `/api/v1/` 아래는 모두 v1, `/api/v1/admin/` 아래는 어드민 전용이라는 규칙을 한 번에 정리하고자 했습니다.

**고민과 해결**  
UserController·ProductController의 RequestMapping을 `/api/v1/users`, `/api/v1/products`로 맞춰 v1 이외의 루트 경로를 없앴습니다. v1·admin Controller에는 @Tag(name = "User API") / @Tag(name = "Admin API")를 붙여 Swagger UI에서 “User API” “Admin API” 두 그룹만 보이게 했습니다. 구 Controller 삭제 후 남는 Controller는 패키지별로만 나뉘어 클래스명이 같아도 빈·문서가 겹치지 않도록 했습니다.

**생산성 향상**  
“이 API는 유저용인지 어드민용인지”를 경로와 Swagger만 보고 판단할 수 있어, 연동·권한 설계 시 혼란이 줄었습니다.

---

### 3.3 로그인·포인트 상세·목록 API와 조회 시점 정합성

**질문**  
> 간편 로그인, 내 포인트 상세(총 잔액·유효 히스토리), 전체 상품·주문 내역·어드민 참여/주문 목록을 추가해 주세요. 포인트 조회 시 서버 시간 기준 만료 건은 Service에서 자동 제외하고, DTO는 전용으로 두며 반복 로직은 private 메서드나 DomainService로 추출해 주세요.

**의도**  
“조회” API를 늘리면서 포인트 총액이 만료를 반영한 값이 되게 하고, 응답은 역할별 전용 DTO만 사용하고 싶었습니다. 유저 조회·포인트 계산 같은 반복은 한 곳으로 모아 유지보수를 쉽게 하고자 했습니다.

**고민과 해결**  
POST /api/v1/auth/login은 닉네임만 받아 loginOrCreate 후 userId·nickname을 반환하고, 이후 요청의 식별자로 userId를 쓰도록 했습니다. GET /api/v1/points/me/{userId}는 조회 직전에 PointDomainService.expireForPoint(point)를 호출해 해당 유저의 만료된 PointDetail을 잔액에서 빼고 applied 처리한 뒤, totalBalance와 유효 PointDetail만으로 히스토리(설명·금액·만료 예정일)를 채웠습니다. 상품 목록·주문 내역·어드민 참여/주문 목록은 전용 QueryService와 전용 DTO로 분리했고, PointsMeService에서는 findPointByUserId, toHistoryItem 같은 private 메서드로 중복을 줄였습니다.

**생산성 향상**  
“포인트 조회할 때 만료 반영이 되는지”를 Service 한 흐름만 보면 확인할 수 있게 되었고, API별 DTO가 나뉘어 불필요한 필드 노출과 스펙 꼬임이 줄었습니다.

---

