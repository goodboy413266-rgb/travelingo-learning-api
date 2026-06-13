package com.travelingo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chapter {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    @Builder.Default
    private String language = "english";

    @Column(length = 50, nullable = false)
    private String category;

    @Column(name = "chapter_no", nullable = false)
    private Integer chapterNo;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "persona_setting", columnDefinition = "TEXT")
    private String personaSetting;

    @Column(name = "total_sessions")
    @Builder.Default
    private Integer totalSessions = 10;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
