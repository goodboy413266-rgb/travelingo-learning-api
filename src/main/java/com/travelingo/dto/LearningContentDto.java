package com.travelingo.dto;

import lombok.*;

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
}
