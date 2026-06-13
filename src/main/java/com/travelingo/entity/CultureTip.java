package com.travelingo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "culture_tip")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CultureTip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "session_no", nullable = false)
    private Integer sessionNo;

    @Column(name = "tip_ko", columnDefinition = "TEXT", nullable = false)
    private String tipKo;

    @Column(name = "tip_en", columnDefinition = "TEXT")
    private String tipEn;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
