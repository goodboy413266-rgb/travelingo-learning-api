package com.travelingo.service;

import com.travelingo.dto.CultureTipDto;
import com.travelingo.entity.CultureTip;
import com.travelingo.repository.CultureTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/**
 * CultureTip 비즈니스 서비스.
 *
 * 설계 의도:
 * - ChapterService, LearningContentService와 동일한 패턴으로 3-layer 분리.
 * - @Transactional(readOnly = true): 조회 전용 → dirty checking 생략, flush 안 함.
 * - 못 찾으면 NoSuchElementException → GlobalExceptionHandler가 404로 변환.
 * - Entity → DTO 변환은 CultureTipDto.from() 정적 팩토리 사용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CultureTipService {

    private final CultureTipRepository cultureTipRepository;

    /**
     * 세션별 문화 팁 조회.
     */
    public CultureTipDto getCultureTip(Long chapterId, Integer sessionNo) {
        CultureTip tip = cultureTipRepository
                .findByChapterIdAndSessionNo(chapterId, sessionNo)
                .orElseThrow(() -> new NoSuchElementException(
                        "문화 팁이 없습니다. chapterId=" + chapterId + ", sessionNo=" + sessionNo));
        return CultureTipDto.from(tip);
    }
}
