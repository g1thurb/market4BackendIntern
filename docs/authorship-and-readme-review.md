# 작업 주체 구분 및 README 정확성 평가

작성일: 2026-11-20 · 대상: `README.md` (커밋 `ddf621a` 기준)

이 문서는 두 가지를 정리한다.

1. **누가 무엇을 했는가** — 프로젝트 오너(본인) / ChatGPT / Claude Code
2. **README가 코드와 실제로 일치하는가** — 정확한 주장, 부정확·과장된 주장, 오래된 내용

---

## 0. 근거의 수준 (먼저 읽을 것)

작업 주체를 구분할 때 근거의 확실성이 서로 다르다. 섞어 쓰면 안 된다.

| 구간 | 근거 | 확실성 |
|---|---|---|
| 2026-11-20 Claude Code 세션 (커밋 `b7c70e6`, `ddf621a`) | 세션 기록, 실행 로그, diff | **확인됨**. 아래 2장의 내용은 직접 확인 가능한 사실 |
| 2026-07-01 ~ 2026-10-06 (커밋 `89c8738` ~ `9487fde`) | git 이력(작성자 `g1thurb`)과 README의 자체 공개 섹션 | **본인 진술**. 커밋 작성자가 본인이라는 것은 확인되지만, 그 안에서 어느 줄을 ChatGPT가 초안 잡았는지는 git으로 구분할 수 없음 |

즉 1장은 README에 이미 본인이 적은 내용을 정리한 것이고, 제3자가 검증한 것이 아니다.

---

## 1. 이번 세션 이전 (2026-07 ~ 2026-10)

### 본인이 한 것 (README 자체 공개 + git 이력)

- 프로젝트 범위와 백엔드 중심 주제 선정
- 관계형 스키마 설계 및 반복 수정 (`db/shoppingMallPortfolioDDL.sql`, `docker/init.sql`)
- 장바구니·주문·쿠폰·재고·주소·주문 상태·스냅샷 비즈니스 규칙 결정
- Entity, Repository, Service, DTO, Controller, 예외 처리 구현
  - 커밋 흐름: `feat: complete initial entity mappings` → `basket vertical done` → `before finallizing order service` → `before redesigning newOrderBasket()`
- 조건부 UPDATE 재고 차감 방식 채택 및 서비스 통합 (`ItemRepository.decreaseAvailable`)
- 로컬 앱·DB 실행, 테스트 실패 해석, 픽스처 문제 수정
- Docker 이미지 / Compose 빌드·실행, Minikube 배포 및 Pod 재생성 수동 확인
- 어떤 제안을 채택·거절할지 최종 결정

코드 곳곳의 한국어 주석과 고민 흔적(예: `UserOrderService.java`의 "previewAvailableItem으로 끝내면 안되나?", "ID를 이렇게 받는게 맞나...")은 본인이 직접 작성·검토했다는 정황이다.

### ChatGPT가 도운 것 (README 자체 공개)

- 스키마·도메인 리뷰, 트랜잭션·주문/쿠폰 관계 트레이드오프 논의
- 테스트할 엣지 케이스 제안
- JUnit / Spring Boot 통합 테스트, MockMvc 테스트 **일부 초안**
- 결정적 픽스처 패턴 제안, 롤백 테스트와 2-스레드 동시성 테스트 **구조 초안**
- Dockerfile, Compose, Minikube 매니페스트 및 명령어 **초안**
- Docker 네트워킹, K8s 개념 설명, 컨테이너 기동 문제 진단 보조
- **README 초안**

README 원문 표현: 일부 테스트·인프라 코드는 본인이 처음부터 타이핑한 것이 아니라 AI 초안이며, 본인이 검토·통합·실행·디버깅했다.

---

## 2. 2026-11-20 Claude Code 세션 (확인된 사실)

### 본인이 한 것

- 작업 목표와 요구사항 제시: 스레드 수 2/10/50/100, 재고 N 케이스, 검증 항목, README 섹션 구성, EXPLAIN(선택), "측정하지 않은 수치는 쓰지 않는다"는 원칙, push 직접 수행
  - 참고: 이 요구사항 텍스트는 다른 AI 도구가 만든 프롬프트를 붙여 넣은 형태였다. 그 프롬프트를 쓰기로 고르고 넘긴 것은 본인이다.
- Claude가 제시한 계획 승인
- 만료된 쿠폰 픽스처를 DB가 아닌 테스트 코드에서 고치는 방안 승인
- 커밋을 두 개로 나누도록 지시
- push는 아직 하지 않음 (본인이 직접 할 예정)

### Claude Code가 한 것

| 작업 | 결과물 |
|---|---|
| 기존 코드 읽기·구조 요약 (테스트, `newOrderItem`, 조건부 UPDATE, Hikari 기본값 확인) | 계획 제시 |
| `feature/concurrency-measurement` 브랜치 생성 | — |
| 동시성 테스트 확장 작성: `@ParameterizedTest`(2/10/50/100), 재고 10/100스레드 케이스, 예상 밖 예외 수집, orders/order_items 행 수 차이 검증, 시간·풀 크기 로그, `show-sql` 끔, `@Order`로 실행 순서 고정 | `UserOrderConcurrencyTest.java` |
| 테스트 반복 실행 및 측정. 첫 실행 케이스가 워밍업을 떠안는 현상을 발견하고 순서를 바꿔 재측정 | 결과 표의 모든 숫자 |
| PostgreSQL 버전·설정, CPU·RAM 조회 | 실험 조건 표 |
| 가짜 데이터(주문 1만 / 주문 항목 10만)를 넣고 인덱스 유무로 `EXPLAIN (ANALYZE, BUFFERS)` 실행 후 ROLLBACK, 원상복구 확인 | 인덱스 비교 표 |
| README "Concurrency verification", "Index check" 섹션 작성, Highlights 한 줄 수정 | `README.md` |
| 전체 테스트 실패 6건 원인 진단 (쿠폰 만료일 2026-10-28 경과). main에서도 같은 실패임을 확인 | — |
| 쿠폰 만료일을 테스트 트랜잭션 안에서 갱신하는 헬퍼 작성 및 두 테스트 클래스에 적용 | `CouponFixtures.java`, `@BeforeEach` 2곳 |
| 전체 테스트 38개 통과 확인, DB 상태 원상 유지 확인 | — |
| 본인 지시에 따라 커밋 2개 생성 (본인 git 계정, `Co-Authored-By: Claude` 표기) | `b7c70e6`, `ddf621a` |
| CV 불릿 문안 작성 | 대화 내 |

### 이번 세션의 성격

이전 공개 섹션은 "ChatGPT는 저장소를 직접 빌드·실행하지 않았다"고 적고 있다. 이번 세션의 Claude Code는 그와 다르다. **직접 코드를 작성하고, 테스트와 SQL을 실행하고, 측정값을 얻고, 커밋했다.** 본인의 역할은 목표 설정, 계획 승인, 방향 결정이었다. 동시성 측정 테스트 코드와 README 측정 섹션의 문장은 본인이 타이핑하지 않았다.

면접에서는 이렇게 말하는 것이 정확하다.

> 동시성 설계(조건부 UPDATE)와 원래의 2-스레드 테스트 통합은 제가 했고, 이를 100스레드까지 확장해 측정하는 작업은 제가 요구사항을 정하고 AI 코딩 에이전트가 구현·실행했습니다. 결과를 검토하고, 워밍업 영향과 커넥션 풀 대기가 왜 생기는지 설명할 수 있습니다.

---

## 3. README 정확성 평가

### 3.1 코드와 일치함 (근거 확인)

| README 주장 | 근거 |
|---|---|
| 조건부 UPDATE로 재고 차감, 0행이면 `InsufficientStockException` | `ItemRepository.java:13`, `UserOrderService.java:468` |
| 장바구니 항목을 item code 순으로 정렬 | `UserOrderService.java:357` |
| Preview는 읽기 전용 | `@Transactional(readOnly = true)` — `UserOrderService.java:77, 204` |
| 판매자별 주문 분리 (Checkout 1 → Order N) | `UserOrderService.java:395`, 테스트 `newOrderBasket_multiStore_success` |
| 부분 실패 시 전체 롤백 테스트 | `UserOrderTransactionTest.basketPartialStockFailure_rollsBackEntireOrder` |
| 스냅샷 테스트: 주문 후 상품명·가격·주소를 바꾸고 원래 값 유지 확인 | `UserOrderServiceTest.snapshot_success` |
| `OrderCoupon`은 `OrderItem`에 연결 | `OrderCoupon.java` `@MapsId` + `order_item_id`, `docker/init.sql:146` |
| 할인액이 소계를 넘지 않음, 할인 한도 적용, 정액·정률 | `UserOrderService.returnDiscountAmount` |
| 재고 부족 409, 잘못된 수량 400, 없는 상품 404, 잘못된 JSON 400 | `GlobalExceptionHandler`, `UserOrderApiControllerTest` |
| Dockerfile 내용 | `Dockerfile`과 동일 |
| K8s: Deployment, NodePort, Secret, ConfigMap 스키마 초기화, `imagePullPolicy: Never` | `k8s/app.yaml`, `k8s/postgres.yaml` |
| Compose의 PostgreSQL은 별도 호스트 포트 사용 | `compose.yaml` 5434 vs 기본 5433 |
| 동시성 측정 표와 EXPLAIN 표의 숫자 | 2026-11-20 세션 실행 로그 |
| 결제 stub, 인증 미완성, K8s 로컬 전용 등 한계 서술 | 코드 상태와 일치 |

### 3.2 부정확하거나 과장된 부분 (수정 권장)

1. **쿠폰 테스트 범위 과장**
   - README 18행(Highlights)과 496행(Testing Strategy)은 "mismatched, duplicate-per-item" 케이스를 검증한 것처럼 읽힌다.
   - 실제 테스트는 `invalidCoupon_fails`(없는 쿠폰), `expiredCoupon_fails`(만료), 쿠폰 적용 성공만 있다.
   - 쿠폰-상품 불일치, 상품당 중복 쿠폰, 미사용 쿠폰 거절, 정률 할인, 할인 한도는 **구현은 있지만 테스트가 없다.**
   - 수정안: "implemented" 와 "tested"를 구분해 적거나, 해당 테스트를 추가한다.

2. **"결정적 픽스처" 주장이 저장소만으로는 성립하지 않음** (20행, 511~532행)
   - 테스트에 필요한 사용자·상품·주소·결제수단·쿠폰 데이터는 저장소의 어떤 SQL에도 없다. 개발자 로컬 DB에 수동으로 들어 있다.
   - `docker/init.sql`은 스키마 전용이고, 그 DB는 포트 5434인데 테스트 기본 접속은 5433이다.
   - 따라서 새로 clone한 환경에서는 `gradlew test`가 통과하지 않는다.
   - 쿠폰 만료일이 절대 날짜로 박혀 있어, 2026-10-28 이후 6개 테스트가 실패했다(`b7c70e6`에서 수정).
   - 수정안: "fixtures are reset per test, but seed rows must exist in the local DB (not included)"처럼 사실대로 적거나, 시드 SQL을 저장소에 추가한다.

3. **"@BeforeEach에서 픽스처 상태를 만든다"** (525행)
   - 장바구니 테스트에는 해당하지만, `UserOrderServiceTest`와 `UserOrderApiControllerTest`에는 `b7c70e6` 이전까지 `@BeforeEach`가 없었다.

4. **"The Gradle test suite is currently passing."** (509행)
   - 이 문장이 쓰인 10월 6일에는 사실이었을 가능성이 높다. 하지만 10월 28일부터 11월 20일 `b7c70e6` 전까지는 사실이 아니었다.
   - 지금은 다시 사실이다(38/38 통과). 다만 2번 문제 때문에 "개발자 로컬 DB에서"라는 조건을 붙이는 것이 정확하다.

5. **AI 공개 섹션이 현재 사실과 다름** (947~995행)
   - ChatGPT만 언급되어 있고 Claude Code 세션이 빠져 있다.
   - 993행 "AI는 저장소를 자율적으로 빌드·실행하지 않았다"는 문장은 ChatGPT에 대해서만 맞다. 이번 세션의 측정·커밋 작업에는 맞지 않는다.
   - 965행 "본인이 전체 테스트를 통과할 때까지 실행"도 이번 세션의 통과 확인은 Claude가 실행한 것이다.
   - 수정안: 2장 내용을 요약해 "Claude Code (2026-11-20)" 하위 섹션을 추가한다.

### 3.3 오래된 내용 (README 외 포함)

1. **README 241행** "Two worker threads..." — 지금 테스트는 2~100스레드다. 바로 아래 Concurrency verification 섹션이 있어 틀린 말은 아니지만, "originally"를 붙이거나 문장을 합치면 깔끔하다.
2. **`db/shoppingMallPortfolioDDL.sql:278`** — `order_coupons`가 아직 `order_id` PK/FK로 되어 있다. 실제 DB와 `docker/init.sql`은 `order_item_id`다. 이 DDL 파일로 DB를 만들면 `ddl-auto: validate`에서 기동에 실패한다.
3. **`UserOrderService.java:21-31`** TODO — "스키마 불일치로 컨텍스트 로딩/테스트가 실패한다"고 적혀 있지만 지금은 컨텍스트가 뜨고 전체 테스트가 통과한다. 해결된 TODO이므로 지우거나 갱신해야 한다.
4. **`docs/implementation-plan.md`** — 로그인·회원가입 페이지/API 계획이 남아 있지만 README 범위(인증은 범위 밖)와 맞지 않는다. 과거 계획임을 표시하면 오해가 없다.

---

## 4. 요약

- **본인이 주도한 핵심**: 도메인·스키마 설계, 서비스 로직, 조건부 UPDATE 선택, 트랜잭션·스냅샷 설계, 로컬·Docker·Minikube 실행과 검증.
- **AI가 크게 기여한 부분**: 테스트와 인프라 설정의 초안(ChatGPT), README 초안(ChatGPT), 100스레드 동시성 측정 테스트와 측정·문서화·쿠폰 픽스처 수정의 구현과 실행(Claude Code).
- **README에서 고쳐야 할 것**: 쿠폰 테스트 범위 과장, 저장소만으로는 재현되지 않는 픽스처, AI 공개 섹션에 Claude Code 누락. 이 세 가지는 면접에서 질문받으면 바로 드러나는 부분이니 먼저 고치는 것이 좋다.
