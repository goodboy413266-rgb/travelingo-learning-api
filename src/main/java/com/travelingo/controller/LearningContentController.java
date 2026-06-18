package com.travelingo.controller;

import com.travelingo.service.LearningContentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 학습 콘텐츠 컨트롤러
 * GET /api/contents?chapterId=1&sessionNo=1  → 세션별 콘텐츠
 * GET /api/contents/all?chapterId=1           → 챕터 전체 콘텐츠
 *
 * 비즈니스 로직과 트랜잭션 경계는 LearningContentService에 위임.
 * @Validated: @RequestParam 단일 파라미터 검증 활성화.
 */
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Validated
public class LearningContentController {

    private final LearningContentService contentService;

    // ========== 세션별 학습 콘텐츠 ==========
    @GetMapping
    public ResponseEntity<?> getContents(
            @RequestParam @Positive Long chapterId,
            @RequestParam @Positive Integer sessionNo) {
        return ResponseEntity.ok(contentService.getContents(chapterId, sessionNo));
    }

    // ========== 챕터 전체 콘텐츠 ==========
    @GetMapping("/all")
    public ResponseEntity<?> getAllByChapter(@RequestParam @Positive Long chapterId) {
        return ResponseEntity.ok(contentService.getAllByChapter(chapterId));
    }
}
