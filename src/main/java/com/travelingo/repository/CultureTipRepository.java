package com.travelingo.repository;

import com.travelingo.entity.CultureTip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CultureTipRepository extends JpaRepository<CultureTip, Long> {
    Optional<CultureTip> findByChapterIdAndSessionNo(Long chapterId, Integer sessionNo);
}
