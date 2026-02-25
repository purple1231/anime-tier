package hello.anime_tier.service;

import hello.anime_tier.domain.search.service.KoreanDivideService;
import hello.anime_tier.domain.search.service.SentenceSplitService;
import hello.anime_tier.domain.search.service.TagSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 통합 테스트입니다!
 */
@SpringBootTest
@Transactional
class TagSearchIntergrationTest {

    @Autowired
    private TagSearchService tagSearchService;

    @Autowired
    private SentenceSplitService sentenceSplitService;

    @Autowired
    private KoreanDivideService koreanDivideService;

    @Test
    @DisplayName("테스트 1: 영어 문장 분리 기능 검증")
    void step1_SplitSentences() {
        //given
        String input = "Please recommend an action animation. Also, I like comedy.";

        //when
        List<String> sentences = sentenceSplitService.split(input);

        //then
        System.out.println("📝 분리된 영어 문장: " + sentences);
        assertThat(sentences).hasSize(2);
        assertThat(sentences).containsExactly(
                "Please recommend an action animation.",
                "Also, I like comedy."
        );
    }

    @Test
    @DisplayName("테스트 2: 한국어 문장 분리 기능 검증")
    void step2_SplitKoreanSentences() {
        //given
        String input = "액션 애니메이션 추천해줘. 그리고 코미디도 좋아해.";

        //when
        List<String> sentences = koreanDivideService.split(input);

        //then
        System.out.println("📝 분리된 한국어 문장: " + sentences);
        assertThat(sentences).hasSize(2);
        assertThat(sentences).containsExactly(
                "액션 애니메이션 추천해줘.",
                "그리고 코미디도 좋아해."
        );
    }

    @Test
    @DisplayName("테스트 3: 임베딩 및 DB 검색을 통한 후보 태그 추출 검증 (AI 호출 제외)")
    void step3_ExtractCandidateTagsFromDB() {
        //given
        String input = "Any anime recommendations for high school girls in a band.";

        //when
        // extractTopTags는 private 메서드이므로 ReflectionTestUtils 사용
        List<String> candidateTags = ReflectionTestUtils.invokeMethod(
                tagSearchService, "extractTopTags", input
        );

        //then
        System.out.println("🔍 DB에서 추출된 후보 태그: " + candidateTags);
        assertThat(candidateTags).isNotEmpty();
    }

//    @Test
//    @DisplayName("테스트 4: 전체 흐름 검증 (임베딩 -> DB검색 -> AI필터링)")
//    void step4_FullFlow_EmbeddingToLLM() {
//        //given
//        String input = "I want to watch a touching anime about music and friendship.";
//
//        //when
//        // 이 메서드가 내부적으로 extractTopTags(임베딩+DB) -> askToAwsAi(LLM) -> divideResponse(파싱) 다 수행함
//        List<String> finalTags = tagSearchService.extractTopFinalTags(input);
//
//        //then
//        System.out.println("🎉 최종 추천 태그: " + finalTags);
//        assertThat(finalTags).isNotEmpty();
//
//        // 프롬프트에서 3개를 요청했으므로 3개가 와야 하지만, AI 특성상 오차가 있을 수 있어 1~5개로 검증
//        assertThat(finalTags.size()).isBetween(1, 5);
//    }
}