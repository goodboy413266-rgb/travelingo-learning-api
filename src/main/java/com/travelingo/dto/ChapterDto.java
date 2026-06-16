package com.travelingo.dto;

import com.travelingo.entity.Chapter;

/**
 * Chapter 응답 DTO
 * - Entity 직접 노출하지 않기 위해 분리
 * - personaSetting(AI 페르소나 프롬프트), createdAt 등 내부/불필요 필드 제외
 * - record로 불변 응답 객체 표현 (Java 14+)
 */
public record ChapterDto(
        Long id,
        String language,
        String category,
        Integer chapterNo,
        String title,
        Integer totalSessions
) {

    /**
     * Entity → DTO 변환 static factory.
     * 변환 로직을 DTO 안에 둬서 Controller/Service가 단순해진다.
     */
    public static ChapterDto from(Chapter chapter) {
        return new ChapterDto(
                chapter.getId(),
                chapter.getLanguage(),
                chapter.getCategory(),
                chapter.getChapterNo(),
                chapter.getTitle(),
                chapter.getTotalSessions()
        );
    }
}
