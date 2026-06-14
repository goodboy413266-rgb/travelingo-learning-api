# 개발 일지 — Travelingo Learning Content API

## 이 문서의 목적

트레블링고 팀 프로젝트에서 담당했던 학습 콘텐츠 매니저 영역(Chapter / LearningContent / CultureTip)을 면접 전에 다시 분석하면서, 코드를 읽고 발견한 점·고민·결정·작업 회고를 일자별로 기록하는 학습 일지.

PLAN.md의 12개 작업 우선순위(P1~P12)를 진행하면서 이 문서에 회고를 누적.

---

## Day 1 (2026-06-13) — 코드 베이스 분석

### 오늘 한 일

- 어제 만든 학습 리포(README/PLAN)를 분석·리팩토링 톤으로 재정리 (commit `5cd35d2`)
- 본인 담당 영역 14개 파일을 분석 베이스로 import (commit `2dbbbbe`)
- 코드 읽으면서 발견한 개선 영역 13개 정리 (이 문서)

### 분석 대상

import한 파일 총 14개:
- entity 3개: `Chapter`, `LearningContent`, `CultureTip`
- repository 3개: `ChapterRepository`, `LearningContentRepository`, `CultureTipRepository`
- controller 3개: `ChapterController`, `LearningContentController`, `CultureTipController`
- dto 1개: `LearningContentDto`
- config 1개: `GlobalExceptionHandler`
- sql 3개: `schema.sql`, `data.sql`, `ch1_data.sql`

Java 코드 합쳐서 약 262줄. 처음 봤을 때 받은 인상은 "표준적인 Spring Boot CRUD"인데, 한 줄씩 따라가니 안티패턴이 여러 개 보였다.

### 도메인 이해

`schema.sql` 먼저 읽고 도메인 잡았다.

- **`chapter`** — 여행 시나리오 챕터(Ch1~Ch10). 컬럼: `language`, `category`(공항/호텔/식당), `chapter_no`, `title`, `persona_setting`(AI 페르소나용), `total_sessions`(기본 10)
- **`learning_content`** — 챕터 안의 세션별 학습 콘텐츠. `type` enum 3가지(WORD / EXPRESSION / QNA). 단어/표현/문답이 같은 테이블에 들어감(`priority`로 우선순위 1=높음 3=낮음)
- **`culture_tip`** — 세션별 문화 매너 팁. 한국어(`tip_ko` NOT NULL), 영어(`tip_en` nullable)

관계: `chapter -< learning_content`(1:N), `chapter -< culture_tip`(1:N). 모두 `chapter_id` FK + `ON DELETE CASCADE`.

처음 헷갈렸던 점: `learning_content`에 `qna_q_en/qna_a_en/qna_q_ko/qna_a_ko` 4컬럼이 있는데 type=QNA일 때만 사용, WORD/EXPRESSION이면 NULL. 한 테이블에 3유형을 다 넣은 설계. 정규화 측면에선 깔끔하지 않지만, 콘텐츠 양 적고 type별 응답 형식이 일관되니 실용적 선택으로 이해.

---

### 발견한 개선 영역 13개

면접에서 "이 부분 왜 이렇게 됐죠?" 질문 받으면 답할 수 있도록 본인 언어로 정리. Critical → High → Medium → Low 순.

#### 🔴 Critical — 안티패턴 (즉시 개선)

**1. Service 레이어 부재**

세 Controller 모두 `xxxRepository`를 직접 주입받아 호출한다. 비즈니스 로직 들어갈 자리가 없고, 결정적으로 **`@Transactional` 어노테이션이 단 한 곳도 없다**. 트랜잭션 경계가 명시되지 않는다는 뜻.

당시에는 "단순 GET API라 트랜잭션 굳이 필요해?"라고 생각했던 것 같은데, 이제 보니 `readOnly = true`로 명시하는 게 Hibernate에게 "dirty checking 안 해도 됨"을 알려주는 최적화 신호라는 걸 알게 됐다. 그리고 향후 POST/PUT 늘어나면 즉시 필요.

→ **P5 작업**. Chapter → LearningContent → CultureTip 순으로 Service 분리.

**2. `.orElse(null)` + null 체크 안티패턴**

`ChapterController` L33-36:

```java
Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
if (chapter == null) {
    return ResponseEntity.status(404).body(Map.of("error", "챕터를 찾을 수 없습니다"));
}
```

`CultureTipController` L29-34도 같은 패턴.

문제 인식:
- Optional을 도입한 의도(null 가능성을 타입으로 표현)를 정면으로 거스른다.
- `GlobalExceptionHandler`가 이미 `NoSuchElementException` 처리 중인데(L17-23) 활용 안 함.
- `.orElseThrow(() -> new NoSuchElementException("..."))`로 바꾸면 코드 줄고 응답 통일.

→ **P2 작업** (내일 제일 먼저).

**3. ChapterController가 Entity를 그대로 응답**

L27: `return ResponseEntity.ok(chapters);` — `List<Chapter>` 그대로
L37: `return ResponseEntity.ok(chapter);` — `Chapter` 그대로

Chapter는 LAZY 관계가 없어서 즉시 폭발은 안 하지만:
- DB 스키마 변경 = API breaking change
- 다른 Controller(`LearningContentController`)는 DTO 변환하는데 `ChapterController`만 안 함 = 일관성 깨짐
- 향후 양방향 관계 도입 시 순환 참조 폭발

→ **P3 작업**. `ChapterDto` 신설.

**4. CultureTipController — DTO 없음, Map 인라인 변환**

L36-39:

```java
return ResponseEntity.ok(Map.of(
    "tipKo", tip.getTipKo(),
    "tipEn", tip.getTipEn() != null ? tip.getTipEn() : ""
));
```

- `LearningContentDto`는 만들었으면서 `CultureTipDto`는 안 만든 일관성 깨짐
- null 처리 로직이 응답 직렬화 자리에 박힘 (Service 자리에 있어야)
- Swagger 문서화 시 스키마 추론 불가

→ **P4 작업**.

---

#### 🟡 High — 학습 가치 + 면접 어필 큼

**5. 단위 테스트 0개**

본인 담당 영역에 테스트 단 한 줄도 없음. 면접 단골 "테스트 어떻게 짜셨어요?"에 답할 게 없는 상태. Repository는 `@DataJpaTest`, Service는 Mockito로 짤 예정.

→ **P7 작업**.

**6. 입력 검증 부재**

`@RequestParam Long chapterId`에 `@Positive`, `@NotNull` 등 검증 어노테이션 0개. 음수 ID나 null이 그대로 Repository까지 내려간다.

→ **P6 작업**. `@Valid` + Bean Validation 적용 + `GlobalExceptionHandler`에 `MethodArgumentNotValidException` 핸들러 추가.

**7. N+1 잠재 위험**

`LearningContent`와 `CultureTip`이 `@ManyToOne(fetch = FetchType.LAZY)`로 `Chapter` 참조. 현재 코드는 DTO 변환 시 chapter 안 건드려서 안 터지지만, 향후 "챕터 제목까지 같이 응답"이 요구되면 즉시 N+1.

→ Service 도입 + Fetch Join 또는 `@EntityGraph` 학습. P5와 P7에서 같이 다룬다.

---

#### 🟢 Medium — 개선 가치 있음

**8. `GlobalExceptionHandler`의 `RuntimeException` 처리가 위험**

L33-39:

```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<?> handleRuntime(RuntimeException e) {
    return ResponseEntity.badRequest().body(  // 무조건 400!
        Map.of("error", message)
    );
}
```

`NullPointerException`(서버 버그)이 400 Bad Request로 위장됨 → 클라이언트가 "내가 잘못 보낸 거구나" 오해. 실제로는 500이어야.

→ **P8 작업**. `RuntimeException` 일반은 500으로, `IllegalArgumentException` 같은 명시적 클라이언트 잘못만 400.

**9. Magic String `"english"` 하드코딩**

`ChapterController` L25: `@RequestParam(defaultValue = "english") String language`. enum 또는 상수 클래스로 분리.

**10. 로깅 부재**

`@Slf4j` 0개. `log.info`, `log.error` 흔적 없음. 디버깅/운영 모니터링 어려움.

→ **P9 작업**.

**11. 페이징 없음**

`findByChapterId` 류가 `List` 반환. `Pageable` 미적용. 학습 콘텐츠가 늘어나면 한 번에 다 내려옴.

→ **P10 작업**.

---

#### 🔵 Low — 시간 남으면

**12. JavaDoc / OpenAPI 어노테이션 부족**

Controller에 클래스/메서드 설명은 있는데 Repository, Entity는 거의 없음. Swagger UI 떠도 빈약.

→ **P11 작업**.

**13. README의 본인 담당 영역 설명 부족**

이 development-log에 정리하고, Day 13에 README도 보강.

→ **P12 작업**.

---

### Day 1 작업 회고

오늘 가장 크게 깨달은 점:
- 처음 본인이 코드 짤 때는 "Controller에서 Repository 바로 호출"이 너무 자연스러웠다. **Service 레이어 빠진 게 가장 큰 약점**이라는 걸 다시 보니 알겠다. 그때는 빠른 결과물 만드는 데 집중하느라 layer 분리를 못 했던 것 같다.
- `.orElse(null)` + null 체크 — Optional을 일반 null처럼 다룬 패턴. `orElseThrow`로 바꾸면 GlobalExceptionHandler가 자동 처리하는 메커니즘이 더 깔끔하다는 걸 새로 인식.
- ChapterController만 Entity 직접 노출 — 같은 프로젝트 안에서 일관성이 깨진 것도 처음엔 못 봤다. 다시 읽으니 명백.

내일(Day 2)부터 **P2 — `.orElse(null)` → `orElseThrow()` 리팩토링**부터 시작. 작은 단위로 워밍업해서 IntelliJ Git UI에 익숙해지는 것도 목표.

### Day 1 commit

```
2dbbbbe  chore: 학습 콘텐츠 매니저 영역 코드 import (분석 베이스)
5cd35d2  docs: 프로젝트 방향 정리 - 트레블링고 분석/리팩토링 학습 리포
29d173a  chore: 프로젝트 초기 셋업 - 학습 계획 문서 추가
0db0e96  feat: Spring Boot 초기 골격 + application.yml 셋업
```

오늘은 분석 + 정리 위주라 코드 변경 commit은 없음. 내일부터 본격 리팩토링.

---

*(Day 2 이후 작업하면서 누적 작성 예정)*
