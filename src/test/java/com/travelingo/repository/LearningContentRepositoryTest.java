package com.travelingo.repository;

import com.travelingo.entity.Chapter;
import com.travelingo.entity.LearningContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * LearningContentRepository 단위 테스트.
 *
 * 사용 어노테이션:
 * - @DataJpaTest          : JPA 관련 Bean만 로드 (가볍고 빠른 슬라이스 테스트)
 *                           각 테스트 후 트랜잭션 롤백.
 * - @AutoConfigureTestDatabase(replace = NONE) : application.yml의 H2 in-memory 그대로 사용.
 *
 * 테스트 전략:
 * - 시드 데이터(schema.sql/data.sql) 영향을 받지 않도록 새 Chapter 생성 후 그 id로 조회.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class LearningContentRepositoryTest {

    @Autowired
    private LearningContentRepository contentRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void 챕터_세션별_콘텐츠를_조회한다() {
        // given - 시드와 안 겹치도록 새 Chapter 생성 (language 길이 20자 제한)
        String testLang = "tlc" + (System.nanoTime() % 1_000_000);
        Chapter chapter = Chapter.builder()
                .language(testLang).category("호텔")
                .chapterNo(1).title("Hotel").totalSessions(2).build();
        em.persist(chapter);

        LearningContent c1 = LearningContent.builder()
                .chapter(chapter).sessionNo(1).sessionName("체크인")
                .type(LearningContent.Type.WORD)
                .contentEn("check-in").contentKo("체크인").build();
        LearningContent c2 = LearningContent.builder()
                .chapter(chapter).sessionNo(1).sessionName("체크인")
                .type(LearningContent.Type.EXPRESSION)
                .contentEn("I'd like to check in").contentKo("체크인하고 싶어요").build();
        LearningContent c3 = LearningContent.builder()
                .chapter(chapter).sessionNo(2).sessionName("체크아웃")
                .type(LearningContent.Type.WORD)
                .contentEn("check-out").contentKo("체크아웃").build();
        em.persist(c1);
        em.persist(c2);
        em.persist(c3);
        em.flush();

        // when - sessionNo=1 만 조회
        List<LearningContent> result =
                contentRepository.findByChapterIdAndSessionNo(chapter.getId(), 1);

        // then - 2개 반환 (sessionNo=2는 제외)
        assertThat(result).hasSize(2);
        assertThat(result).extracting(LearningContent::getSessionNo)
                .containsOnly(1);
    }

    @Test
    void 챕터별_전체_콘텐츠를_조회한다() {
        // given
        String testLang = "tla" + (System.nanoTime() % 1_000_000);
        Chapter chapter = Chapter.builder()
                .language(testLang).category("공항")
                .chapterNo(1).title("Airport").totalSessions(2).build();
        em.persist(chapter);

        LearningContent c1 = LearningContent.builder()
                .chapter(chapter).sessionNo(1).sessionName("입국심사")
                .type(LearningContent.Type.WORD)
                .contentEn("immigration").contentKo("입국심사").build();
        LearningContent c2 = LearningContent.builder()
                .chapter(chapter).sessionNo(2).sessionName("수하물")
                .type(LearningContent.Type.WORD)
                .contentEn("baggage").contentKo("수하물").build();
        em.persist(c1);
        em.persist(c2);
        em.flush();

        // when - chapterId만 조회
        List<LearningContent> result = contentRepository.findByChapterId(chapter.getId());

        // then - 2개 모두 반환
        assertThat(result).hasSize(2);
    }
}
