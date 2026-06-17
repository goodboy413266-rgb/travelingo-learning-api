package com.travelingo.dto;

import com.travelingo.entity.LearningContent;
import lombok.*;

/**
 * LearningContent 응답 DTO.
 * - Entity 직접 노출하지 않기 위해 분리
 * - ChapterDto, CultureTipDto와 동일하게 static factory from() 제공
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningContentDto {
    private Long id;
    private Integer sessionNo;
    private String sessionName;
    private String type;       // WORD, EXPRESSION, QNA
    private Integer priority;
    private String contentEn;
    private String contentKo;
    private String exampleEn;
    private String exampleKo;
    private String qnaQEn;
    private String qnaAEn;
    private String qnaQKo;
    private String qnaAKo;

    /**
     * Entity → DTO 변환 static factory.
     * 변환 로직을 DTO 안에 둬서 Service/Controller가 단순해진다.
     */
    public static LearningContentDto from(LearningContent c) {
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
