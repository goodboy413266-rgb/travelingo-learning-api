package com.travelingo.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 전역 예외 처리기 - 모든 Controller에서 발생하는 예외를 잡아서 깔끔한 JSON 응답 반환
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
