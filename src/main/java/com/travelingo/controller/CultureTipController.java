package com.travelingo.controller;

import com.travelingo.entity.CultureTip;
import com.travelingo.repository.CultureTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travelingo.dto.CultureTipDto;

import java.util.NoSuchElementException;

/**
 * 문화 팁 컨트롤러
 * GET /api/culture-tips?chapterId=1&sessionNo=1
 */
@RestController
@RequestMapping("/api/culture-tips")
@RequiredArgsConstructor
public class CultureTipController {

    private final CultureTipRepository cultureTipRepository;

    // ========== 세션별 문화 팁 ==========
    @GetMapping
    public ResponseEntity<?> getCultureTip(
            @RequestParam Long chapterId,
            @RequestParam Integer sessionNo) {

        CultureTip tip = cultureTipRepository
                .findByChapterIdAndSessionNo(chapterId, sessionNo)
                .orElseThrow(() -> new NoSuchElementException(
                        "문화 팁이 없습니다. chapterId=" + chapterId + ", sessionNo=" + sessionNo));

        return ResponseEntity.ok(CultureTipDto.from(tip));
    }
}
