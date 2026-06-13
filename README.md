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
| 테스트 | JUnit 5, Spring Boot Test, AssertJ |

## 빠른 실행

### 로컬 (H2 in-memory) — 기본

```bash
./gradlew bootRun
```
- API 서버: http://localhost:8080
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
- [docs/INTERVIEW_NOTES.md](docs/INTERVIEW_NOTES.md) — JPA·N+1·OSIV 등 면접 예상 질문 정리 *(Day 13 작성 예정)*

## 작업 이력

리팩토링·테스트·문서화 작업 내역은 commit log와 development-log.md 참조.

## License

MIT © 2026 JJH
