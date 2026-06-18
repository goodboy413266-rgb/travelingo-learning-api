package com.travelingo.repository;

import com.travelingo.entity.Chapter;
import com.travelingo.entity.CultureTip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * CultureTipRepository 단위 테스트.
 *
 * 사용 어노테이션:
 * - @DataJpaTest          : JPA 관련 Bean만 로드 (가볍고 빠른 슬라이스 테스트)
 *                           각 테스트 후 트랜잭션 롤백.
 * - @AutoConfigureTestDatabase(replace = NONE) : application.yml의 H2 in-memory 그대로 사용.
 *
 * 테스트 전략:
 * - 시드 데이터(schema.sql/data.sql) 영향을 받지 않도록 새 Chapter 생성 후 그 id로 조회.
 * - Optional 반환 메서드의 두 가지 경우 모두 검증 (값 있음 / 없음).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class CultureTipRepositoryTest {

    @Autowired
    private CultureTipRepository cultureTipRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void 챕터_세션별_문화팁을_조회한다() {
        // given - 시드와 안 겹치도록 새 Chapter 생성 (language 길이 20자 제한)
        String testLang = "tct" + (System.nanoTime() % 1_000_000);
        Chapter chapter = Chapter.builder()
                .language(testLang).category("호텔")
                .chapterNo(1).title("Hotel").totalSessions(1).build();
        em.persist(chapter);

        CultureTip tip = CultureTip.builder()
                .chapter(chapter).sessionNo(1)
                .tipKo("미국 호텔은 체크인 시 신용카드가 필요합니다")
                .tipEn("US hotels require a credit card at check-in")
                .build();
        em.persist(tip);
        em.flush();

        // when
        Optional<CultureTip> result =
                cultureTipRepository.findByChapterIdAndSessionNo(chapter.getId(), 1);

        // then - 값 존재, 내용 일치
        assertThat(result).isPresent();
        assertThat(result.get().getTipKo()).contains("호텔");
        assertThat(result.get().getSessionNo()).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_세션_조회시_빈_Optional을_반환한다() {
        // given - Chapter만 만들고 CultureTip은 안 만듦
        String testLang = "tcn" + (System.nanoTime() % 1_000_000);
        Chapter chapter = Chapter.builder()
                .language(testLang).category("공항")
                .chapterNo(1).title("Airport").totalSessions(1).build();
        em.persist(chapter);
        em.flush();

        // when - sessionNo=99 (존재 X)
        Optional<CultureTip> result =
                cultureTipRepository.findByChapterIdAndSessionNo(chapter.getId(), 99);

        // then - 빈 Optional
        assertThat(result).isEmpty();
    }
}
