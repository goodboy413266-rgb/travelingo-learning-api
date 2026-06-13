# Travelingo Learning Content API — 작업 계획

> 작성: 2026-06-13
> 목표 완료: 2026-06-24 (면접 직전)

## 1. 프로젝트 의미

트레블링고 팀 프로젝트의 **학습 콘텐츠 매니저 영역**(Chapter / LearningContent / CultureTip) 코드를 본인 손으로 분석·리팩토링하면서 본인 명의 commit으로 작업 이력을 정상화하는 개인 학습 리포.

세부 분석 결과는 [docs/development-log.md](development-log.md) 참조.

## 2. 기술 스택

- Spring Boot 3.5.11 / Java 21 / Gradle 8.14
- Spring Data JPA / Hibernate / MySQL (캠퍼스) + H2 (로컬)
- Lombok / Spring Validation / Springdoc OpenAPI
- JUnit 5 / Spring Boot Test / AssertJ / Mockito

## 3. 작업 우선순위 12개

코드 분석에서 도출한 13개 개선 영역을 작업 단위로 묶어 우선순위로 정렬.

| 순위 | 작업 | 예상 commit |
|---|---|---|
| P1 | 코드 분석 일지 작성 (development-log.md) | 1-2 |
| P2 | `.orElse(null)` → `orElseThrow()` 리팩토링 | 2 |
| P3 | ChapterDto 분리 (Entity 직접 노출 제거) | 2 |
| P4 | CultureTipDto 분리 (Map 인라인 제거) | 1 |
| P5 | Service 레이어 도입 (Chapter → LearningContent → CultureTip) | 3-4 |
| P6 | @Valid 입력 검증 + 검증 실패 응답 통일 | 2 |
| P7 | 단위 테스트 (Repository @DataJpaTest, Service Mockito) | 3-4 |
| P8 | GlobalExceptionHandler RuntimeException 분류 개선 | 1 |
| P9 | @Slf4j 로깅 추가 | 1 |
| P10 | Pageable 적용 | 2 |
| P11 | Swagger 어노테이션 (@Operation, @Schema) | 1 |
| P12 | README 정리 + docs/INTERVIEW_NOTES.md 작성 | 1-2 |

총 22~23 commit 예상.

## 4. 일정 (12일)

| Day | 날짜 | 작업 |
|---|---|---|
| 1 | 06-13 | 문서 정리 + 코드 import + development-log.md (P1) |
| 2 | 06-14 | P2 — orElseThrow 리팩토링 |
| 3 | 06-15 | P3 — ChapterDto 분리 |
| 4 | 06-16 | P4 — CultureTipDto 분리 |
| 5-7 | 06-17~19 | P5 — Service 레이어 도입 |
| 8 | 06-20 | P6 — @Valid 입력 검증 |
| 9-10 | 06-21~22 | P7 — 단위 테스트 |
| 11 | 06-23 | P8 + P9 — 예외 처리 + 로깅 |
| 12 | 06-23 | P10 + P11 — Pageable + Swagger |
| 13 | 06-24 | P12 + INTERVIEW_NOTES + 최종 빌드 |

## 5. 커밋 규칙

- 형식: `[타입]: 한 줄 요약`
- 타입: `feat` / `fix` / `refactor` / `test` / `docs` / `chore`
- 하루 1~3 commit. 의미 단위로 분할.
- 예시:
  - `refactor: ChapterController .orElse(null) → orElseThrow 교체`
  - `test: ChapterRepository @DataJpaTest 단위 테스트 추가`
  - `feat: ChapterDto 도입 (Entity 직접 노출 제거)`

## 6. 면접 대비 (Day 13에 정리)

JPA 기반 백엔드 면접 단골:
1. JPA 영속성 컨텍스트 / dirty checking
2. `@ManyToOne(fetch = LAZY)` 선택 이유 (N+1 문제)
3. Entity vs DTO — 왜 Entity 직접 노출하면 안 되나
4. `@Transactional` 범위 / `readOnly = true` 효과
5. Optional + `orElseThrow`의 의도
6. `@RestControllerAdvice` 전역 예외 처리 동작
7. 생성자 주입 (`@RequiredArgsConstructor`) 선호 이유
8. Bean Validation (`@Valid`, `@NotNull` 등) 적용 패턴

→ Day 13에 `docs/INTERVIEW_NOTES.md` 작성.

## 7. Done 기준

- [ ] `./gradlew bootRun` 실행되고 H2 연결됨
- [ ] `./gradlew test` 통과 (테스트 다 그린)
- [ ] Swagger UI(`/swagger-ui.html`)에서 API 호출 가능
- [ ] `docs/development-log.md` Day별 일지 완성
- [ ] `docs/INTERVIEW_NOTES.md` 면접 예상 질문 8개+ 답안 정리
- [ ] README에 분석 결과 + 작업 이력 요약
- [ ] 본인 단독 author commit 22개+ 자연스러운 페이스
- [ ] 본인 GitHub public 리포 push 완료
