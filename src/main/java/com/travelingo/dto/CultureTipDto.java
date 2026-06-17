package com.travelingo.dto;

import com.travelingo.entity.CultureTip;

/**
 * CultureTip 응답 DTO
 * - Entity 직접 노출하지 않기 위해 분리
 * - 응답에 필요한 tipKo, tipEn 필드만 포함 (id, chapter, sessionNo, createdAt 등 제외)
 * - record로 불변 응답 객체 표현 (Java 14+)
 * - ChapterDto, LearningContentDto와 동일한 패턴으로 일관성 유지
 */
public record CultureTipDto(
        String tipKo,
        String tipEn
) {

    /**
     * Entity → DTO 변환 static factory.
     * tipEn은 null인 경우 빈 문자열("")로 변환하여 응답 일관성 유지.
     */
    public static CultureTipDto from(CultureTip tip) {
        return new CultureTipDto(
                tip.getTipKo(),
                tip.getTipEn() != null ? tip.getTipEn() : ""
        );
    }
}
