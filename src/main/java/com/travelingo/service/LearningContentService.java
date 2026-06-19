package com.travelingo.service;

import com.travelingo.dto.LearningContentDto;
import com.travelingo.repository.LearningContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LearningContent 비즈니스 서비스.
 *
 * 설계 의도:
 * - ChapterService와 동일한 패턴으로 3-layer 분리.
 * - @Transactional(readOnly = true): 조회 전용 → dirty checking 생략, flush 안 함.
 * - Entity → DTO 변환은 LearningContentDto.from() 정적 팩토리 사용.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningContentService {

    private final LearningContentRepository contentRepository;

    /**
     * 세션별 학습 콘텐츠 조회.
     */
    public List<LearningContentDto> getContents(Long chapterId, Integer sessionNo) {
        log.info("getContents 호출 chapterId={}, sessionNo={}", chapterId, sessionNo);
        List<LearningContentDto> result = contentRepository.findByChapterIdAndSessionNo(chapterId, sessionNo)
                .stream()
                .map(LearningContentDto::from)
                .toList();
        log.debug("getContents 결과 size={}", result.size());
        return result;
    }

    /**
     * 챕터 전체 학습 콘텐츠 조회.
     */
    public List<LearningContentDto> getAllByChapter(Long chapterId) {
        log.info("getAllByChapter 호출 chapterId={}", chapterId);
        List<LearningContentDto> result = contentRepository.findByChapterId(chapterId)
                .stream()
                .map(LearningContentDto::from)
                .toList();
        log.debug("getAllByChapter 결과 size={}", result.size());
        return result;
    }
}
