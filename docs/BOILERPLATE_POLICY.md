# Boilerplate 정책 (프로젝트 베이스 규칙)

이 문서는 이 보일러플레이트를 베이스로 하는 **모든 프로젝트**가 따를 아키텍처·레이어·정책을 정의합니다.

---

## 1. 아키텍처 원칙

- **헥사고날(포트·어댑터)** + **레이어드** 구조를 유지한다.
- **도메인**은 외부 의존성이 없고, **application**은 도메인 + port만 의존한다.
- **infra**만 프레임워크(Spring, DB, 외부 API)에 의존한다.

---

## 2. 패키지/레이어 규칙

| 레이어 | 패키지 | 역할 |
|--------|--------|------|
| **domain** | `domain.model.<도메인>` | 엔티티, VO(value class). 비즈니스 규칙은 VO `init` 등에 반영 |
| **application** | `application.port.in` | UseCase 인터페이스, Command |
| | `application.port.out` | DB/외부 연동 Port (LoadXxxPort, CommandXxxPort 등) |
| | `application.port.access` | 조회/스냅샷용 Port (XxxAccessPort, XxxResult, XxxSnapshot) |
| | `application.facade` | Port 구현체. Port 조합·트랜잭션 경계 |
| | `application.service` | **UseCase 구현체**. Controller가 주입받는 진입점 |
| | `application.validator` | 도메인 규칙 검증 |
| **infra** | `infra.adapter.in` | Controller, DTO, Exception Advice |
| | `infra.adapter.out` | JPA, 외부 API, 파일, 이벤트 발행 등 |
| | `infra.config` | Security, JPA, Swagger, WebMvc 등 |
| **shared** | `shared.exception`, `response`, `util` 등 | 예외, 응답 코드, 유틸, 공용 DTO |

- **프로젝트 패키지 prefix**는 **한 가지로 통일**한다 (예: `com.krince.boilerplate`). 다른 프로젝트 이름(예: prizm)이 코드에 남지 않도록 한다.

---

## 3. UseCase ↔ ApplicationService 정책

- **Controller**는 **UseCase 인터페이스**만 주입받는다.
- **UseCase 구현체**는 **ApplicationService**가 담당한다.
- ApplicationService는 Facade 또는 Port를 호출하고, **Command → 도메인 타입 변환**은 ApplicationService에서 한다.

```
Controller → GetUserUseCase (interface)
              ↑
              UserApplicationService (implements GetUserUseCase)
              → UserFacade.findById(UserId) → UserResult
```

- 새 기능 추가 시: **UseCase 인터페이스** → **ApplicationService에서 구현** → Controller에서 UseCase만 주입.

---

## 4. API · 보안 정책

- **API prefix**: `/api` (예: `/api/users`, `/api/auth/tokens`).
- **인증**: JWT, **Authorization 헤더(Bearer)** 사용. 쿠키 기반 인증은 사용하지 않는다.
- **Security permit 목록**: Swagger, actuator, 정적 리소스, 로그인/리프레시/로그아웃 URL만 permit. **문서/리소스 경로는 프로젝트명과 무관하게** `/swagger-ui/**`, `/v3/api-docs/**` 등으로 통일한다.

---

## 5. 응답 · 예외 정책

- **성공**: `SuccessResponse<T>` (code, message, data, success, status, detailCode).
- **실패**: `ExceptionResponse` + `ExceptionResponseCode` (상세 코드). `GlobalExceptionHandler`에서 일괄 처리.
- **detailCode**는 **문자열** (예: `"200_000"`, `"BR-000"`). JSON/설정에서 문자열로 직렬화할 때 따옴표를 빠뜨리지 않는다.

---

## 6. 테스트 · 품질 정책

- **JaCoCo** 커버리지 최소 **80%** 유지.
- **테스트 스타일**: JUnit 5 + Kotest, MockK, RestAssured. FISS2 가이드(Given-When-Then, 구체 값 사용 등) 준수.
- Controller 테스트 시 인증은 **Authorization 헤더(Bearer)** 로 처리.

---

## 7. 새 프로젝트 생성 시 체크리스트

1. 패키지 prefix를 새 프로젝트명으로 일괄 변경 (예: `com.krince.boilerplate` → `com.krince.새프로젝트`).
2. SwaggerConfig 등에서 **다른 프로젝트 패키지명** 검색 후 제거 또는 변경.
3. SecurityConfig의 permit URL이 **문서/리소스만** 열려 있는지 확인.
4. UseCase는 ApplicationService에서 구현되어 있고, Controller는 UseCase만 주입받는지 확인.
5. README에 실행 방법, 환경 변수, 프로파일, 테스트/커버리지 명령 정리.

---

이 정책은 보일러플레이트 분석 결과를 반영한 것이며, 모든 파생 프로젝트의 공통 베이스로 적용한다.
