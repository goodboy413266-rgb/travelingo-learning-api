package com.travelingo.controller;

import com.travelingo.dto.LearningContentDto;
import com.travelingo.entity.LearningContent;
import com.travelingo.repository.LearningContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 학습 콘텐츠 컨트롤러
 * GET /api/contents?chapterId=1&sessionNo=1  → 세션별 콘텐츠
 * GET /api/contents/all?chapterId=1           → 챕터 전체 콘텐츠
 */
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class LearningContentController {

    private final LearningContentRepository contentRepository;

    // ========== 세션별 학습 콘텐츠 ==========
    @GetMapping
    public ResponseEntity<?> getContents(
            @RequestParam Long chapterId,
            @RequestParam Integer sessionNo) {

        List<LearningContent> contents = contentRepository
            .findByChapterIdAndSessionNo(chapterId, sessionNo);

        // Entity → DTO 변환 (stream API)
        List<LearningContentDto> dtoList = contents.stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // ========== 챕터 전체 콘텐츠 ==========
    @GetMapping("/all")
    public ResponseEntity<?> getAllByChapter(@RequestParam Long chapterId) {
        List<LearningContent> contents = contentRepository.findByChapterId(chapterId);
        List<LearningContentDto> dtoList = contents.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // Entity → DTO 변환 헬퍼
    private LearningContentDto toDto(LearningContent c) {
        return LearningContentDto.builder()
            .id(c.getId())
            .sessionNo(c.getSessionNo())
            .sessionName(c.getSessionName())
            .type(c.getType().name())
            .priority(c.getPriority())
            .contentEn(c.getContentEn())
            .contentKo(c.getContentKo())
            .exampleEn(c.getExampleEn())
            .exampleKo(c.getExampleKo())
            .qnaQEn(c.getQnaQEn())
            .qnaAEn(c.getQnaAEn())
            .qnaQKo(c.getQnaQKo())
            .qnaAKo(c.getQnaAKo())
            .build();
    }
}
