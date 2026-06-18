package com.travelingo.controller;

import com.travelingo.service.CultureTipService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 문화 팁 컨트롤러
 * GET /api/culture-tips?chapterId=1&sessionNo=1
 *
 * 비즈니스 로직과 트랜잭션 경계는 CultureTipService에 위임.
 * @Validated: @RequestParam 단일 파라미터 검증 활성화.
 */
@RestController
@RequestMapping("/api/culture-tips")
@RequiredArgsConstructor
@Validated
public class CultureTipController {

    private final CultureTipService cultureTipService;

    // ========== 세션별 문화 팁 ==========
    @GetMapping
    public ResponseEntity<?> getCultureTip(
            @RequestParam @Positive Long chapterId,
            @RequestParam @Positive Integer sessionNo) {
        return ResponseEntity.ok(cultureTipService.getCultureTip(chapterId, sessionNo));
    }
}
