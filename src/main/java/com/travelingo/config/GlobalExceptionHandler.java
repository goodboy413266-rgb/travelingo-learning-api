package com.travelingo.config;

import jakarta.validation.ConstraintViolationException;
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
 * - RuntimeException                   → 400 (기타 클라이언트 잘못)
 * - Exception                          → 500 (서버 오류)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // orElseThrow()에서 발생하는 NoSuchElementException → 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(NoSuchElementException e) {
        String message = e.getMessage() != null ? e.getMessage() : "요청한 리소스를 찾을 수 없습니다";
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
        return ResponseEntity.badRequest().body(
            Map.of("error", "날짜 형식이 올바르지 않습니다", "detail", e.getMessage())
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        String message = e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다";
        return ResponseEntity.badRequest().body(
            Map.of("error", message)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        String detail = e.getMessage() != null ? e.getMessage() : "상세 정보 없음";
        return ResponseEntity.internalServerError().body(
            Map.of("error", "서버 오류가 발생했습니다", "detail", detail)
        );
    }
}
