package hello.anime_tier.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 통합 테스트입니다!
 */
@SpringBootTest
@Transactional
class TagSearchIntergrationTest {

    @Autowired
    private TagSearchService tagSearchService;

    @Test
    @DisplayName("테스트 1: 임베딩 및 DB 검색을 통한 후보 태그 추출 검증 (AI 호출 제외)")
    void step1_ExtractCandidateTagsFromDB() {
        //given
        String input = "Any anime recommendations for high school girls in a band.";

        //when
        List<String> candidateTags = ReflectionTestUtils.invokeMethod(
                tagSearchService, "extractTopTags", input
        );

        //then
        System.out.println("🔍 DB에서 추출된 후보 태그: " + candidateTags);


    }


}
