package com.travelingo.config;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 전역 예외 처리기 - 모든 Controller에서 발생하는 예외를 잡아서 깔끔한 JSON 응답 반환.
 *
 * 처리 매핑:
 * - NoSuchElementException             → 404 (리소스 없음, orElseThrow에서 발생)
 * - ConstraintViolationException       → 400 (@RequestParam/@PathVariable 검증 실패, @Validated 클래스)
 * - MethodArgumentNotValidException    → 400 (@RequestBody @Valid 객체 검증 실패, 향후 POST/PUT 대비)
 * - DateTimeParseException             → 400 (날짜 파싱 오류)
 * - IllegalArgumentException           → 400 (잘못된 인자 - 클라이언트 잘못)
 * - RuntimeException                   → 500 (NPE 등 예측 못 한 서버 오류)
 * - Exception                          → 500 (체크드 예외 포함 서버 오류)
 *
 * 변경 이력:
 * - 초반에는 RuntimeException을 400으로 처리했으나, NullPointerException 등
 *   진짜 서버 잘못도 400으로 나가는 문제가 있었음.
 *   → 예측 가능한 클라이언트 잘못은 IllegalArgumentException으로 명시하고,
 *     나머지 RuntimeException은 500으로 분리.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // orElseThrow()에서 발생하는 NoSuchElementException → 404
    // 404는 정상적인 흐름의 일부이므로 INFO 레벨로 기록 (예: 잘못된 id 조회는 흔한 케이스).
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(NoSuchElementException e) {
        String message = e.getMessage() != null ? e.getMessage() : "요청한 리소스를 찾을 수 없습니다";
        log.info("리소스 없음 (404): {}", message);
        return ResponseEntity.status(404).body(
            Map.of("error", message)
        );
    }

    // @RequestParam, @PathVariable 단일 파라미터 검증 실패 → 400
    // 예: @Positive Long chapterId 에 -1이 들어오면 발생
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        List<String> violations = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        log.warn("파라미터 검증 실패 (400): {}", violations);
        return ResponseEntity.badRequest().body(
            Map.of(
                "error", "입력값이 유효하지 않습니다",
                "violations", violations
            )
        );
    }

    // @RequestBody @Valid 객체 검증 실패 → 400 (향후 POST/PUT body 검증 대비)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Body 검증 실패 (400): {}", fieldErrors);
        return ResponseEntity.badRequest().body(
            Map.of(
                "error", "입력값이 유효하지 않습니다",
                "fieldErrors", fieldErrors
            )
        );
    }

    // 날짜 파싱 오류 → 400
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParse(DateTimeParseException e) {
        log.warn("날짜 파싱 실패 (400): {}", e.getMessage());
        return ResponseEntity.badRequest().body(
            Map.of("error", "날짜 형식이 올바르지 않습니다", "detail", e.getMessage())
        );
    }

    // 잘못된 인자(클라이언트 잘못) → 400
    // 예: Service에서 if (id < 0) throw new IllegalArgumentException(...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        String message = e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다";
        log.warn("잘못된 인자 (400): {}", message);
        return ResponseEntity.badRequest().body(
            Map.of("error", message)
        );
    }

    // 위에서 잡히지 않은 모든 RuntimeException → 500
    // 예: NullPointerException, ClassCastException, ArithmeticException 등
    // → 클라이언트 잘못이 아니라 서버 코드의 버그이므로 500이 맞다.
    // → 스택트레이스까지 ERROR 레벨로 남겨야 운영 환경에서 추적 가능.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        String detail = e.getMessage() != null ? e.getMessage() : "상세 정보 없음";
        log.error("예상치 못한 서버 오류 (500): {}", detail, e);
        return ResponseEntity.internalServerError().body(
            Map.of("error", "서버 오류가 발생했습니다", "detail", detail)
        );
    }

    // 체크드 예외 포함 최후의 방어선 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        String detail = e.getMessage() != null ? e.getMessage() : "상세 정보 없음";
        log.error("처리되지 않은 예외 (500): {}", detail, e);
        return ResponseEntity.internalServerError().body(
            Map.of("error", "서버 오류가 발생했습니다", "detail", detail)
        );
    }
}
