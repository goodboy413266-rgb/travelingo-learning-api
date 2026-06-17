package com.travelingo.service;

import com.travelingo.dto.ChapterDto;
import com.travelingo.entity.Chapter;
import com.travelingo.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Chapter 비즈니스 서비스.
 *
 * 설계 의도:
 * - Controller와 Repository 사이에 Service 레이어를 두어 3-layer 분리.
 *   (Controller: 요청/응답 매핑, Service: 비즈니스 로직 + 트랜잭션, Repository: DB 접근)
 * - 조회 전용 메서드만 있어서 클래스 레벨에 @Transactional(readOnly = true) 적용.
 *   → Hibernate dirty checking 생략 (성능 최적화 신호)
 *   → flush 호출 안 함
 * - Entity → DTO 변환도 Service에서 수행하여 Controller는 응답 반환만 담당.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterService {

    private final ChapterRepository chapterRepository;

    /**
     * 언어별 챕터 목록 조회.
     */
    public List<ChapterDto> getChapters(String language) {
        return chapterRepository.findByLanguageOrderByChapterNo(language)
                .stream()
                .map(ChapterDto::from)
                .toList();
    }

    /**
     * 챕터 단건 조회.
     * 못 찾으면 NoSuchElementException → GlobalExceptionHandler가 404로 변환.
     */
    public ChapterDto getChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("챕터를 찾을 수 없습니다. id=" + chapterId));
        return ChapterDto.from(chapter);
    }
}
