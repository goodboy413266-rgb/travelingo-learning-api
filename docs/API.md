# REST API 명세

> 각 Day 별 작업하면서 채워나감. 면접에서 보여줄 핵심 문서.

## Base URL
- 로컬: `http://localhost:8080`
- Swagger UI (Day 11~): `http://localhost:8080/swagger-ui.html`

## 인증
없음 (학습 프로젝트, 인증은 범위 밖).

---

## 엔드포인트 목록

### Chapter

| Method | Path | 설명 | Day |
|---|---|---|---|
| `GET` | `/api/chapters` | 전체 챕터 조회 | 3 |
| `GET` | `/api/chapters/{id}` | 단일 챕터 조회 | 3 |

*(상세 스펙은 Day 3 작성)*

### LearningContent

| Method | Path | 설명 | Day |
|---|---|---|---|
| `GET` | `/api/contents?chapterId={id}&sessionNo={n}` | 세션별 콘텐츠 | 5 |
| `GET` | `/api/contents/all?chapterId={id}` | 챕터 전체 콘텐츠 | 5 |

*(상세 스펙은 Day 5 작성)*

### CultureTip

| Method | Path | 설명 | Day |
|---|---|---|---|
| `GET` | `/api/tips?chapterId={id}&sessionNo={n}` | 세션별 문화 팁 | 6 |

*(상세 스펙은 Day 6 작성)*

---

## 공통 응답 형식

### 성공
*(Day 5 작성: 단일 객체 / 배열 형태)*

### 에러
*(Day 10 작성: GlobalExceptionHandler 적용 후)*
```json
{
  "timestamp": "2026-06-20T00:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Chapter not found: id=99",
  "path": "/api/chapters/99"
}
```

---

## 페이징 / 정렬 (Day 8 적용 예정)

`Spring Data JPA Pageable` 사용.

쿼리 파라미터:
- `page` (default 0)
- `size` (default 20)
- `sort` (예: `chapterNo,asc`)

응답 형식:
```json
{
  "content": [ ... ],
  "totalElements": 90,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

---

## 검증 규칙 (Day 10 적용 예정)

각 DTO에 `@Valid`, `@NotNull`, `@Size`, `@Min` 등 적용.
검증 실패 시 400 + 필드별 메시지.

---

## 변경 이력

| 날짜 | Day | 변경 |
|---|---|---|
| 2026-06-11 | 1 | 초안 골격 작성 (엔드포인트 목록 placeholder) |
