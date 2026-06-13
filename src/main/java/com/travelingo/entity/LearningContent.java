package com.travelingo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_content")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningContent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "session_no", nullable = false)
    private Integer sessionNo;

    @Column(name = "session_name", length = 50, nullable = false)
    private String sessionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Builder.Default
    private Integer priority = 1;

    @Column(name = "content_en", nullable = false)
    private String contentEn;

    @Column(name = "content_ko", nullable = false)
    private String contentKo;

    @Column(name = "example_en", columnDefinition = "TEXT")
    private String exampleEn;

    @Column(name = "example_ko", columnDefinition = "TEXT")
    private String exampleKo;

    @Column(name = "qna_q_en", columnDefinition = "TEXT")
    private String qnaQEn;

    @Column(name = "qna_a_en", columnDefinition = "TEXT")
    private String qnaAEn;

    @Column(name = "qna_q_ko", columnDefinition = "TEXT")
    private String qnaQKo;

    @Column(name = "qna_a_ko", columnDefinition = "TEXT")
    private String qnaAKo;

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type { WORD, EXPRESSION, QNA }
}
