package hello.anime_tier.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TagSearchServiceTest {

    private TagSearchService tagSearchService = new TagSearchService(null, null, null);


    @Test
    @DisplayName("여러 문장을 문단으로 잘 쪼개는지 테스트")
    void splitIntoSentenceTest(){
        //given
        String input = "Please recommend an action animation where a bald guy breaks everything down.";
        //when
        List<String> result = ReflectionTestUtils.invokeMethod(
                tagSearchService, "splitIntoSentences", input
        );
        //private 한 함수를 테스트코드에서 임의로 접근가능하게 하는 기술

        //then

        result.forEach(System.out::println);
    }


    @Test
    @DisplayName("대답이 잘 나눠지는지")
    void divideResponseisGood(){
        //given
        String input = "{안녕}, {나는}, {김진}, {1} 처럼 결과가 나왔습니다!";

        //when
        List<String> result = ReflectionTestUtils.invokeMethod(
                tagSearchService, "divideResponse", input
        );

        result.forEach(System.out::println);
    }







}