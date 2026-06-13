package com.travelingo.repository;

import com.travelingo.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByLanguageOrderByChapterNo(String language);
}
