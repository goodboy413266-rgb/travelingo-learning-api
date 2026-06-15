package com.travelingo.controller;

import com.travelingo.entity.Chapter;
import com.travelingo.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 챕터 컨트롤러
 * GET /api/chapters              → 언어별 챕터 목록
 * GET /api/chapters/{chapterId}  → 챕터 상세
 */
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterRepository chapterRepository;

    // ========== 언어별 챕터 목록 ==========
    @GetMapping
    public ResponseEntity<?> getChapters(@RequestParam(defaultValue = "english") String language) {
        List<Chapter> chapters = chapterRepository.findByLanguageOrderByChapterNo(language);
        return ResponseEntity.ok(chapters);
    }

    // ========== 챕터 상세 ==========
    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getChapter(@PathVariable Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("챕터를 찾을 수 없습니다. id=" + chapterId));
        return ResponseEntity.ok(chapter);
    }
}
