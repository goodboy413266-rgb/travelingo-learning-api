# travelingo-learning-api

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-6DB33F?logo=spring&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.14.5-02303A?logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

> 트레블링고 팀 프로젝트의 **학습 콘텐츠 매니저 영역**(Chapter / LearningContent / CultureTip)을 분석·리팩토링하면서 본인 명의 commit으로 작업 이력을 정리하는 개인 학습 리포.

---

## 이 프로젝트의 위치

트레블링고 팀 프로젝트(`greatai2025-design/travelingo_v9`)에서 **학습 콘텐츠 매니저 영역**을 담당했으나, 당시 git 활용이 미숙해 본인 명의 commit으로 작업 흐름이 깨끗하게 남지 않았다.

면접 전에 같은 영역 코드를 다시 분석하고, 안티패턴 리팩토링·DTO 정리·Service 레이어 분리·단위 테스트·문서화를 진행하면서 작업 이력을 정상화한다.

작업 내역 전체는 commit log와 [docs/development-log.md](docs/development-log.md) 참조.

## 다루는 도메인

여행 시나리오(공항·호텔·식당 등)별로 학습 콘텐츠(단어·표현·문답)와 현지 문화 팁을 관리·제공하는 REST API의 백엔드 일부.

| 도메인 | 역할 |
|---|---|
| `Chapter` | 여행 시나리오 챕터 (Ch1~Ch10, 카테고리=공항/호텔/식당 등) |
| `LearningContent` | 챕터별 학습 콘텐츠 (단어 / 표현 / Q&A 3유형) |
| `CultureTip` | 세션별 현지 문화 매너 팁 |

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| 언어 / 빌드 | Java 21, Gradle 8.14 |
| 프레임워크 | Spring Boot 3.5.11 |
| 데이터 | Spring Data JPA, Hibernate |
| DB (개발) | H2 in-memory |
| DB (배포) | MySQL 8 (캠퍼스 서버) |
| 부가 | Lombok, Spring Validation, Springdoc OpenAPI |
| 로깅 | SLF4J + Logback (`@Slf4j`) |
| 테스트 | JUnit 5, Spring Boot Test, Mockito, AssertJ |

## 아키텍처

**3-layer 분리 + DTO 패턴**으로 책임을 명확히 나눴다.

```
HTTP 요청
  ↓
[Controller]  @RestController · 요청/응답 매핑 · 파라미터 검증(@Validated)
  ↓
[Service]     @Service · 비즈니스 로직 · 트랜잭션 경계(@Transactional)
  ↓
[Repository]  Spring Data JPA · DB 접근
  ↓
[Entity]  ↔  DTO (record + static factory `from()`)
```

- **DTO 분리**: Entity를 외부에 직접 노출하지 않아 내부 필드(`personaSetting` 같은 AI 프롬프트, `createdAt` 등) 노출 방지
- **조회 전용 메서드**: 클래스 레벨 `@Transactional(readOnly = true)`로 dirty checking 생략

## API 엔드포인트

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/chapters?language=english` | 언어별 챕터 목록 |
| GET | `/api/chapters/{chapterId}` | 챕터 단건 조회 |
| GET | `/api/contents?chapterId=1&sessionNo=1` | 세션별 학습 콘텐츠 |
| GET | `/api/contents/all?chapterId=1` | 챕터 전체 콘텐츠 |
| GET | `/api/culture-tips?chapterId=1&sessionNo=1` | 세션별 문화 팁 |

상세 명세는 [docs/API.md](docs/API.md) 또는 실행 후 Swagger UI 확인.

## 예외 처리 정책

`@RestControllerAdvice` 기반 `GlobalExceptionHandler`로 일관된 JSON 응답을 보장.

| 예외 | HTTP | 로그 레벨 | 책임 |
|---|---|---|---|
| `NoSuchElementException` | 404 | INFO | 정상 흐름 (잘못된 id 조회는 흔함) |
| `ConstraintViolationException` | 400 | WARN | `@RequestParam`/`@PathVariable` 검증 실패 |
| `MethodArgumentNotValidException` | 400 | WARN | `@RequestBody @Valid` 검증 실패 |
| `IllegalArgumentException` | 400 | WARN | 잘못된 인자 (클라이언트 잘못) |
| `DateTimeParseException` | 400 | WARN | 날짜 파싱 오류 |
| `RuntimeException` | **500** | ERROR (+stacktrace) | NPE 등 예측 못한 서버 오류 |
| `Exception` | 500 | ERROR (+stacktrace) | 체크드 예외 최후의 방어선 |

> 초반에는 `RuntimeException`을 400으로 처리했으나, NPE 같은 서버 버그도 400으로 나가는 문제가 있어 500으로 분리. 예측 가능한 클라이언트 잘못은 `IllegalArgumentException`으로 명시.

## 테스트 전략

두 가지 레벨로 분리하여 책임을 명확히 한다.

| 종류 | 어노테이션 | 대상 | 속도 |
|---|---|---|---|
| Repository 슬라이스 테스트 | `@DataJpaTest` | JPA 쿼리 메서드 · SQL 매핑 | 느림 (H2 기동) |
| Service 단위 테스트 | `@ExtendWith(MockitoExtension.class)` | 비즈니스 로직 · 예외 흐름 | 빠름 (ms 단위) |

- BDD 스타일 (`given` / `when` / `then`)
- 단언은 AssertJ (`assertThat`, `assertThatThrownBy`)
- Mockito `BDDMockito.then().should()`로 호출 횟수까지 검증

```bash
./gradlew test
```

## 빠른 실행

### 로컬 (H2 in-memory) — 기본

```bash
./gradlew bootRun
```
- API 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 콘솔: http://localhost:8080/h2-console

### 캠퍼스 MySQL 프로파일

```powershell
$env:DB_USERNAME = "..."
$env:DB_PASSWORD = "..."
./gradlew bootRun --args='--spring.profiles.active=campus'
```

### 빌드 + 테스트

```bash
./gradlew build
./gradlew test
```

## 문서

- [docs/PLAN.md](docs/PLAN.md) — 작업 우선순위 + 일정
- [docs/development-log.md](docs/development-log.md) — 코드 분석 일지 + 작업 회고
- [docs/API.md](docs/API.md) — REST API 명세
- [docs/INTERVIEW_NOTES.md](docs/INTERVIEW_NOTES.md) — JPA·N+1·OSIV 등 면접 예상 질문 정리

## 작업 이력

리팩토링·테스트·문서화 작업 내역은 commit log와 [docs/development-log.md](docs/development-log.md) 참조.

## License

MIT © 2026 JJH
