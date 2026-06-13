package com.travelingo.repository;

import com.travelingo.entity.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {
    List<LearningContent> findByChapterIdAndSessionNo(Long chapterId, Integer sessionNo);
    List<LearningContent> findByChapterId(Long chapterId);
}
