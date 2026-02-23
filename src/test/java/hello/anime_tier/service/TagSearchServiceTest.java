package hello.anime_tier.service;

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

@SpringBootTest
@Transactional
class TagSearchServiceTest {

    @Autowired
    private TagSearchService tagSearchService;

    @Autowired
    private SentenceSplitService sentenceSplitService;

    @Test
    @DisplayName("여러 문장을 문단으로 잘 쪼개는지 테스트")
    void splitIntoSentenceTest(){
        //given
        String input = "Please recommend an action animation where a bald guy breaks everything down.";
        
        //when
        // TagSearchService 내부에서 사용하는 TextSplitter를 통해 분리 로직이 수행됨
        // 여기서는 SentenceSplitService를 직접 호출하여 테스트하거나, 
        // TagSearchService의 extractTopTags 내부 로직을 간접적으로 테스트해야 함.
        // 하지만 단위 테스트 관점에서 분리 로직 자체를 검증하려면 SentenceSplitService를 테스트하는 것이 맞음.
        
        List<String> result = sentenceSplitService.split(input);

        //then
        result.forEach(System.out::println);
        assertThat(result).isNotEmpty();
    }


    @Test
    @DisplayName("대답이 잘 나눠지는지")
    void divideResponseisGood(){
        //given
        String input = "{안녕}, {나는}, {김진}, {1} 처럼 결과가 나왔습니다!";

        //when
        // divideResponse는 private 메서드이므로 ReflectionTestUtils 사용 유지
        List<String> result = ReflectionTestUtils.invokeMethod(
                tagSearchService, "divideResponse", input
        );

        //then
        result.forEach(System.out::println);
        assertThat(result).containsExactly("안녕", "나는", "김진", "1");
    }
}