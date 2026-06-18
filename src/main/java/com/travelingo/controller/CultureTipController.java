package com.travelingo.controller;

import com.travelingo.service.CultureTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 문화 팁 컨트롤러
 * GET /api/culture-tips?chapterId=1&sessionNo=1
 *
 * 비즈니스 로직과 트랜잭션 경계는 CultureTipService에 위임.
 * Controller는 요청 매핑 + 응답 반환만 담당.
 */
@RestController
@RequestMapping("/api/culture-tips")
@RequiredArgsConstructor
public class CultureTipController {

    private final CultureTipService cultureTipService;

    // ========== 세션별 문화 팁 ==========
    @GetMapping
    public ResponseEntity<?> getCultureTip(
            @RequestParam Long chapterId,
            @RequestParam Integer sessionNo) {
        return ResponseEntity.ok(cultureTipService.getCultureTip(chapterId, sessionNo));
    }
}
