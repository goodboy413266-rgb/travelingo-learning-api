package com.travelingo.service;

import com.travelingo.dto.ChapterDto;
import com.travelingo.entity.Chapter;
import com.travelingo.repository.ChapterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * ChapterService 순수 단위 테스트.
 *
 * 사용 어노테이션:
 * - @ExtendWith(MockitoExtension.class) : JUnit 5에 Mockito 통합 (init/verify 자동화)
 * - @Mock                               : ChapterRepository를 가짜(stub)로 만든다.
 * - @InjectMocks                        : @Mock으로 만든 객체를 ChapterService에 생성자 주입.
 *
 * 테스트 전략:
 * - Spring 컨텍스트를 띄우지 않아 DataJpaTest보다 훨씬 빠르다 (밀리초 단위).
 * - Repository 동작은 가짜로 정해두고, Service의 변환/예외 로직만 검증한다.
 * - given - when - then BDD 스타일로 의도를 명확히 한다.
 */
@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @InjectMocks
    private ChapterService chapterService;

    @Test
    @DisplayName("언어별 챕터 목록을 ChapterDto 리스트로 변환해 반환한다")
    void getChapters_언어별_챕터_목록_반환() {
        // given - Repository가 Chapter 2개를 돌려준다고 가정
        Chapter ch1 = Chapter.builder()
                .id(1L).language("english").category("공항")
                .chapterNo(1).title("Airport").totalSessions(10).build();
        Chapter ch2 = Chapter.builder()
                .id(2L).language("english").category("호텔")
                .chapterNo(2).title("Hotel").totalSessions(10).build();

        given(chapterRepository.findByLanguageOrderByChapterNo("english"))
                .willReturn(List.of(ch1, ch2));

        // when
        List<ChapterDto> result = chapterService.getChapters("english");

        // then - Entity → DTO 매핑이 정확히 됐는지
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Airport");
        assertThat(result.get(1).title()).isEqualTo("Hotel");

        // Repository가 정확히 1번 호출됐는지
        then(chapterRepository).should().findByLanguageOrderByChapterNo("english");
    }

    @Test
    @DisplayName("해당 언어 챕터가 없으면 빈 리스트를 반환한다")
    void getChapters_빈_리스트_반환() {
        // given
        given(chapterRepository.findByLanguageOrderByChapterNo("french"))
                .willReturn(List.of());

        // when
        List<ChapterDto> result = chapterService.getChapters("french");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("챕터 단건 조회 성공 시 ChapterDto를 반환한다")
    void getChapter_단건_조회_성공() {
        // given
        Chapter chapter = Chapter.builder()
                .id(1L).language("english").category("공항")
                .chapterNo(1).title("Airport").totalSessions(10).build();

        given(chapterRepository.findById(1L)).willReturn(Optional.of(chapter));

        // when
        ChapterDto result = chapterService.getChapter(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Airport");
        assertThat(result.category()).isEqualTo("공항");
    }

    @Test
    @DisplayName("존재하지 않는 챕터 조회 시 NoSuchElementException을 던진다")
    void getChapter_없으면_예외() {
        // given - Repository가 빈 Optional을 돌려준다
        given(chapterRepository.findById(99L)).willReturn(Optional.empty());

        // when & then - orElseThrow에서 예외 발생
        assertThatThrownBy(() -> chapterService.getChapter(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");

        // findById는 호출됐지만, 후속 동작(DTO 변환)은 없어야 한다
        then(chapterRepository).should().findById(99L);
        then(chapterRepository).should(never()).findByLanguageOrderByChapterNo(anyString());
    }
}
