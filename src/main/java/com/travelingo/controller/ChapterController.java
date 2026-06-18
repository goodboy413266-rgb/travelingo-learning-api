package com.travelingo.controller;

import com.travelingo.service.ChapterService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 챕터 컨트롤러
 * GET /api/chapters              → 언어별 챕터 목록
 * GET /api/chapters/{chapterId}  → 챕터 상세
 *
 * 비즈니스 로직과 트랜잭션 경계는 ChapterService에 위임.
 * @Validated: @RequestParam/@PathVariable 단일 파라미터 검증 활성화.
 *   → 검증 실패 시 ConstraintViolationException 발생 → GlobalExceptionHandler가 400 처리.
 */
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Validated
public class ChapterController {

    private final ChapterService chapterService;

    // ========== 언어별 챕터 목록 ==========
    @GetMapping
    public ResponseEntity<?> getChapters(
            @RequestParam(defaultValue = "english") @NotBlank String language) {
        return ResponseEntity.ok(chapterService.getChapters(language));
    }

    // ========== 챕터 상세 ==========
    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getChapter(@PathVariable @Positive Long chapterId) {
        return ResponseEntity.ok(chapterService.getChapter(chapterId));
    }
}
