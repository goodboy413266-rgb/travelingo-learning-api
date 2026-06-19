# 면접 예상 질문 및 답변 노트

`travelingo-learning-api` 프로젝트 기반으로 정리한 면접 대비 Q&A. 각 질문에 대해 **핵심 답변 → 코드/근거 → 예상 추가 질문**까지 한 묶음으로 준비.

---

## Q1. 3-layer 아키텍처(Controller-Service-Repository)를 왜 분리했나요?

### 핵심 답변

각 레이어의 **책임을 명확히 나누기 위해서**입니다.
- **Controller**: HTTP 요청/응답 매핑, 파라미터 검증
- **Service**: 비즈니스 로직, 트랜잭션 경계 관리
- **Repository**: DB 접근

분리하지 않으면 Controller에 SQL이 섞이고, 테스트가 어려워지며, 트랜잭션 경계가 모호해집니다.

### 코드 근거

```java
// Controller: HTTP만 처리
@GetMapping("/{chapterId}")
public ResponseEntity<?> getChapter(@PathVariable @Positive Long chapterId) {
    return ResponseEntity.ok(chapterService.getChapter(chapterId));
}

// Service: 비즈니스 로직 + 트랜잭션
@Transactional(readOnly = true)
public ChapterDto getChapter(Long chapterId) {
    Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new NoSuchElementException(...));
    return ChapterDto.from(chapter);
}
```

### 예상 추가 질문

- **Q**: "그럼 Service가 없으면 어떤 문제가 생기나요?"
- **A**: 트랜잭션이 Controller나 Repository에 걸리면 의도와 다른 시점에 커밋되거나, 여러 Repository 호출을 묶을 수 없습니다. 또한 비즈니스 로직 단위 테스트도 Spring 컨텍스트가 필요해져 느려집니다.

---

## Q2. DTO를 왜 따로 두나요? record는 왜 쓰셨나요?

### 핵심 답변

**Entity를 외부에 직접 노출하지 않기 위해서**입니다.
- Entity에는 내부 전용 필드(`personaSetting`=AI 프롬프트, `createdAt`)가 있는데, 그대로 노출되면 보안·유지보수 문제가 생깁니다.
- `record`는 Java 14+ 기능으로 **불변(immutable) + 보일러플레이트 제거**가 가능합니다. 응답 DTO는 어차피 수정할 일이 없어서 record가 잘 맞습니다.

### 코드 근거

```java
public record ChapterDto(
        Long id, String language, String category,
        Integer chapterNo, String title, Integer totalSessions
) {
    // Entity → DTO 변환 static factory
    public static ChapterDto from(Chapter chapter) {
        return new ChapterDto(
                chapter.getId(), chapter.getLanguage(),
                chapter.getCategory(), chapter.getChapterNo(),
                chapter.getTitle(), chapter.getTotalSessions()
        );
    }
}
```

→ `personaSetting`, `createdAt`은 의도적으로 제외했습니다.

### 예상 추가 질문

- **Q**: "그럼 모든 DTO를 record로 쓰면 되나요?"
- **A**: 응답 DTO는 record가 좋지만, **요청 DTO는 일반 class가 나을 수 있습니다**. record는 setter가 없어서 일부 검증 라이브러리나 Builder 패턴과 잘 안 맞을 수 있기 때문입니다. 실제로 이 프로젝트의 `LearningContentDto`는 필드가 많고 향후 확장 가능성이 있어 class + @Builder로 두었습니다.

---

## Q3. `@Transactional(readOnly = true)`의 효과는?

### 핵심 답변

**조회 전용 트랜잭션 최적화**입니다.
1. **Dirty Checking 생략**: Hibernate가 변경 감지를 위해 영속성 컨텍스트에 스냅샷을 저장하지 않습니다.
2. **flush 자동 호출 안 함**: 트랜잭션 커밋 시점에도 UPDATE 쿼리가 안 나갑니다.
3. **읽기 전용임을 명시**: 동료나 미래의 본인이 코드를 읽을 때 "이건 변경 없음"을 즉시 인지합니다.

### 코드 근거

```java
@Service
@Transactional(readOnly = true)  // 클래스 레벨
public class ChapterService {
    public ChapterDto getChapter(Long id) { ... }  // 모든 조회 메서드가 readOnly
}
```

### 예상 추가 질문

- **Q**: "readOnly = true인데 데이터를 수정하면 어떻게 되나요?"
- **A**: Hibernate가 변경 감지를 안 하니까 UPDATE 쿼리가 안 나갑니다. 코드에서 setter를 호출해도 DB에는 반영되지 않습니다. 다만 예외는 안 던지므로 **버그를 늦게 발견할 수 있어 주의**가 필요합니다.

---

## Q4. N+1 문제는 어떻게 해결하셨나요?

### 핵심 답변

**N+1 문제**는 부모 엔티티 1개를 조회한 뒤 연관된 자식들을 하나씩 추가 쿼리로 가져오는 현상입니다. 예: Chapter 10개 → 각 Chapter의 LearningContent 조회 시 10번 추가 쿼리 = **1+10 = 11번 쿼리**.

해결 방법:
1. **Fetch Join** (`@Query`로 명시): `select c from Chapter c join fetch c.contents`
2. **`@EntityGraph`**: 메서드 레벨에 fetch 대상 지정
3. **Batch Size**: 한 번에 IN 쿼리로 묶기 (`hibernate.default_batch_fetch_size`)

이 프로젝트는 도메인이 단순해 N+1이 발생하지 않지만, 향후 Chapter ↔ LearningContent 같은 연관 관계 조회가 늘어나면 fetch join으로 대응합니다.

### 코드 예시

```java
@Query("SELECT c FROM Chapter c JOIN FETCH c.contents WHERE c.language = :lang")
List<Chapter> findWithContentsByLanguage(@Param("lang") String lang);
```

### 예상 추가 질문

- **Q**: "그럼 fetch join을 모든 곳에 쓰면 되나요?"
- **A**: 아닙니다. **카르테시안 곱 문제**가 발생할 수 있고, **페이징과 함께 쓰면 메모리에서 페이징하는 문제**가 생깁니다. 1:N 관계 fetch join + 페이징은 위험합니다. 그 경우는 Batch Size 방식이 더 안전합니다.

---

## Q5. `@DataJpaTest`와 Mockito 단위 테스트의 차이는?

### 핵심 답변

| 항목 | `@DataJpaTest` (슬라이스) | Mockito (단위) |
|---|---|---|
| 검증 대상 | JPA 쿼리 + SQL 매핑 + 트랜잭션 | Service 로직만 |
| Spring 컨텍스트 | 부분 로드 (JPA 관련만) | 미로드 |
| DB | 실제 H2 in-memory | Mock |
| 속도 | 느림 (초 단위) | 빠름 (밀리초) |

**두 가지 다 필요한 이유**: 각각 책임이 다릅니다. Repository 테스트는 SQL이 진짜 도는지 검증해야 하고, Service 테스트는 비즈니스 로직만 검증해야 하므로 Repository를 mock합니다.

### 코드 근거

```java
// Repository 테스트: 실제 H2 사용
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class ChapterRepositoryTest {
    @Autowired private TestEntityManager em;
    // em.persist() → 실제 SQL 실행
}

// Service 테스트: Repository를 mock
@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {
    @Mock private ChapterRepository chapterRepository;
    @InjectMocks private ChapterService chapterService;
    // given(repo.findById(1L)).willReturn(Optional.of(chapter));
}
```

### 예상 추가 질문

- **Q**: "Service에서 Repository를 mock하면 진짜 SQL 오류는 못 잡지 않나요?"
- **A**: 맞습니다. 그래서 **둘 다 필요**합니다. Repository 슬라이스 테스트에서 SQL이 정확한지 검증하고, Service 단위 테스트에서 비즈니스 로직(예외 흐름, 변환)이 정확한지 검증합니다. 통합 테스트(`@SpringBootTest`)는 더 무거우니 필요한 부분만 추가합니다.

---

## Q6. `GlobalExceptionHandler`에서 `RuntimeException`을 왜 500으로 바꾸셨나요?

### 핵심 답변

처음에는 `RuntimeException`을 400으로 처리했는데, **`NullPointerException` 같은 서버 코드 버그도 400으로 나가는 문제**가 있었습니다. 클라이언트 입장에서는 "내가 잘못 보냈나?" 헷갈리고, 운영 입장에서는 진짜 서버 버그가 묻혔습니다.

그래서 **예측 가능한 클라이언트 잘못은 `IllegalArgumentException`으로 명시**하여 400으로 두고, **나머지 RuntimeException은 서버 책임이므로 500**으로 분리했습니다.

### 코드 근거

```java
// 클라이언트 잘못 → 400
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("잘못된 인자 (400): {}", e.getMessage());
    return ResponseEntity.badRequest().body(...);
}

// 서버 잘못 → 500 (스택트레이스 포함)
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<?> handleRuntime(RuntimeException e) {
    log.error("예상치 못한 서버 오류 (500): {}", e.getMessage(), e);
    return ResponseEntity.internalServerError().body(...);
}
```

### 예상 추가 질문

- **Q**: "그럼 `@ExceptionHandler` 우선순위는 어떻게 되나요?"
- **A**: **더 구체적인 예외가 우선**입니다. `NullPointerException`이 발생하면 `RuntimeException` 핸들러가 잡고, `IllegalArgumentException`이 발생하면 따로 정의한 핸들러가 우선 잡습니다. 그래서 핸들러 메서드 작성 순서가 아니라 **예외 클래스 계층**으로 결정됩니다.

---

## Q7. 로그 레벨(INFO / WARN / ERROR) 분리 기준은?

### 핵심 답변

**누가 보고, 어떤 조치를 해야 하는지**를 기준으로 나눴습니다.

| 레벨 | 기준 | 예시 |
|---|---|---|
| `INFO` | 정상 흐름의 일부, 추적용 | 메서드 호출, 404 (잘못된 id 조회는 흔함) |
| `DEBUG` | 개발 환경에서만 상세 | 결과 size, 내부 상태 |
| `WARN` | 클라이언트 잘못, 조치 불필요 | 400 검증 실패 |
| `ERROR` | 서버 잘못, **즉시 조치** | 500 + 스택트레이스 |

### 코드 근거

```java
// 정상 흐름
log.info("getChapter 호출 chapterId={}", chapterId);

// 404 — 정상 흐름 (잘못된 id는 클라이언트가 흔히 보냄)
log.info("리소스 없음 (404): {}", message);

// 400 — 클라이언트 잘못
log.warn("파라미터 검증 실패 (400): {}", violations);

// 500 — 서버 버그, 스택트레이스 필수
log.error("예상치 못한 서버 오류 (500): {}", detail, e);
```

### 예상 추가 질문

- **Q**: "왜 404를 WARN이 아니라 INFO로 했나요?"
- **A**: 404는 **잘못된 id 조회 같은 흔한 케이스**입니다. 사용자가 URL을 잘못 입력한 것뿐이라 운영자가 따로 조치할 필요가 없습니다. 만약 WARN으로 두면 흔한 케이스 때문에 로그가 시끄러워져서 **진짜 경고가 묻힙니다**.

---

## Q8. `@Validated`와 `@Valid`의 차이는?

### 핵심 답변

둘 다 Bean Validation을 트리거하지만, **대상이 다릅니다**.

| 어노테이션 | 출처 | 대상 |
|---|---|---|
| `@Valid` | Jakarta Validation 표준 | **객체 내부 필드** (예: `@RequestBody` DTO) |
| `@Validated` | Spring 제공 | **클래스 레벨**에서 단일 파라미터 검증 활성화 (`@RequestParam`, `@PathVariable`) |

### 코드 근거

```java
@RestController
@RequestMapping("/api/chapters")
@Validated  // ← 클래스에 붙여야 단일 파라미터 검증 활성화
public class ChapterController {

    @GetMapping
    public ResponseEntity<?> getChapters(
            @RequestParam @NotBlank String language) {  // @Validated 덕분에 동작
        ...
    }

    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getChapter(@PathVariable @Positive Long chapterId) {
        ...
    }
}
```

→ `@Validated` 없이 `@NotBlank`만 붙이면 **무시**됩니다.

### 예상 추가 질문

- **Q**: "그럼 두 예외도 다른가요?"
- **A**: 네. 
  - `@Validated` + 단일 파라미터 검증 실패 → `ConstraintViolationException`
  - `@Valid` + 객체 검증 실패 → `MethodArgumentNotValidException`
  
  그래서 `GlobalExceptionHandler`에서 두 핸들러를 각각 만들었습니다. 응답 메시지에서도 `violations`와 `fieldErrors`로 키를 다르게 두어 클라이언트가 구분할 수 있게 했습니다.

---

## 🎯 마지막 정리 — 면접 직전 체크리스트

- [ ] 모든 답변의 **핵심 키워드**를 1초 안에 떠올릴 수 있나? (트랜잭션 경계, fetch join, Mockito, @Validated 등)
- [ ] **코드 예시**를 화이트보드에 1분 안에 적을 수 있나?
- [ ] **"왜 그렇게 했나요?"** 질문에 의사결정 근거를 댈 수 있나?
- [ ] **"그러면 이런 경우는?"** follow-up에 막히지 않나?
- [ ] 모르는 건 "그 부분은 아직 학습 중입니다"로 솔직하게 답할 마음의 준비?

> 답변할 때는 **결론 → 근거 → 코드** 순서로. 면접관이 "그래서?"라고 기다리게 하지 말 것.

---

## 📚 추가 학습 권장 주제

- [ ] Spring Boot 자동 설정(`@SpringBootApplication` 내부)
- [ ] JPA 영속성 컨텍스트 1차 캐시 / 쓰기 지연
- [ ] `@Transactional` 격리 레벨 / 전파 옵션
- [ ] Spring AOP 동작 원리 (프록시)
- [ ] HTTP 상태 코드 의미 (200/201/204/400/401/403/404/409/500)
