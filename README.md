# Boilerplate

PCN 프로젝트 공통 베이스 애플리케이션. Kotlin + Spring Boot 기반 헥사고날 구조와 인증·응답·테스트 정책을 정의합니다.

## 요구 사항

- JDK 21
- PostgreSQL (선택: PostGIS)
- Gradle 8.x (Wrapper 포함)

## 실행

```bash
# PostgreSQL 실행 후
./gradlew bootRun
```

- 기본 포트: **8080**
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### 환경 설정

- `application.yml`: DB URL, JWT secret, 파일 저장 경로 등.
- 프로파일: `default` 외 `localtest` 등 사용 시 `--spring.profiles.active=localtest`
- CORS: `CORS_ALLOWED_ORIGINS` 환경 변수로 허용 Origin 지정 (미설정 시 `*`)

## 프로젝트 구조 요약

```
src/main/kotlin/com/pcn/boilerplate/
├── domain/          # 도메인 모델, VO
├── application/     # UseCase(port.in), Port(port.out/access), Facade, Service(UseCase 구현)
├── infra/           # Controller, JPA, Security, Swagger, Config
└── shared/          # 예외, 응답 코드, 유틸, 공용 DTO
```

- **Controller** → **UseCase** → **ApplicationService** → Facade/Port → **도메인**
- 인증: JWT **Authorization 헤더(Bearer)**. Swagger에서 Authorize에 토큰 입력 후 사용.

자세한 레이어·정책은 **[docs/BOILERPLATE_POLICY.md](docs/BOILERPLATE_POLICY.md)** 참고.

## 테스트

```bash
# 전체 테스트
./gradlew test

# 커버리지 리포트 (80% 미만 시 빌드 실패)
./gradlew test jacocoTestReport
```

- 리포트: `build/reports/jacoco/test/html/index.html`
- 테스트 스타일: JUnit 5, Kotest, MockK, RestAssured (FISS2 가이드 준수)

## 정책

이 보일러플레이트를 베이스로 할 때 따를 아키텍처·API·보안·테스트 정책은 **[docs/BOILERPLATE_POLICY.md](docs/BOILERPLATE_POLICY.md)** 에 정의되어 있습니다. 새 프로젝트 생성 시 해당 문서와 체크리스트를 따라 주세요.
