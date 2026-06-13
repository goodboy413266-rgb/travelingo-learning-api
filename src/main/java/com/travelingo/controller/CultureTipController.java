package com.travelingo.controller;

import com.travelingo.entity.CultureTip;
import com.travelingo.repository.CultureTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            .orElse(null);

        if (tip == null) {
            return ResponseEntity.status(404).body(Map.of("error", "문화 팁이 없습니다"));
        }

        return ResponseEntity.ok(Map.of(
            "tipKo", tip.getTipKo(),
            "tipEn", tip.getTipEn() != null ? tip.getTipEn() : ""
        ));
    }
}
