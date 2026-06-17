package com.travelingo.controller;

import com.travelingo.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 챕터 컨트롤러
 * GET /api/chapters              → 언어별 챕터 목록
 * GET /api/chapters/{chapterId}  → 챕터 상세
 *
 * 비즈니스 로직과 트랜잭션 경계는 ChapterService에 위임.
 * Controller는 요청 매핑 + 응답 반환만 담당.
 */
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    // ========== 언어별 챕터 목록 ==========
    @GetMapping
    public ResponseEntity<?> getChapters(@RequestParam(defaultValue = "english") String language) {
        return ResponseEntity.ok(chapterService.getChapters(language));
    }

    // ========== 챕터 상세 ==========
    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getChapter(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterService.getChapter(chapterId));
    }
}
