package com.travelingo.repository;

import com.travelingo.entity.Chapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * ChapterRepository 단위 테스트.
 *
 * 사용 어노테이션:
 * - @DataJpaTest          : JPA 관련 Bean만 로드 (가볍고 빠른 슬라이스 테스트)
 *                           각 테스트 후 트랜잭션 롤백.
 * - @AutoConfigureTestDatabase(replace = NONE) : application.yml의 H2 in-memory 그대로 사용.
 *
 * 테스트 전략:
 * - 시드 데이터(schema.sql/data.sql) 영향을 받지 않도록 unique language 사용.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class ChapterRepositoryTest {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void 언어별_챕터_목록을_chapter_no_순서로_조회한다() {
        // given - 시드 데이터와 충돌 안 나도록 unique language 사용 (컬럼 길이 20자 제한)
        String testLang = "tlang" + (System.nanoTime() % 1_000_000);

        Chapter ch2 = Chapter.builder()
                .language(testLang).category("호텔")
                .chapterNo(2).title("Hotel").totalSessions(10).build();
        Chapter ch1 = Chapter.builder()
                .language(testLang).category("공항")
                .chapterNo(1).title("Airport").totalSessions(10).build();
        em.persist(ch2);
        em.persist(ch1);
        em.flush();

        // when - 해당 언어로 조회
        List<Chapter> result = chapterRepository.findByLanguageOrderByChapterNo(testLang);

        // then - 2개 반환, chapter_no 오름차순 (1, 2)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getChapterNo()).isEqualTo(1);
        assertThat(result.get(1).getChapterNo()).isEqualTo(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Airport");
        assertThat(result.get(1).getTitle()).isEqualTo("Hotel");
    }

    @Test
    void 존재하지_않는_언어_조회시_빈_리스트를_반환한다() {
        // given - 존재하지 않는 unique language (컬럼 길이 20자 제한)
        String nonExistent = "noexist" + (System.nanoTime() % 1_000_000);

        // when
        List<Chapter> result = chapterRepository.findByLanguageOrderByChapterNo(nonExistent);

        // then
        assertThat(result).isEmpty();
    }
}
