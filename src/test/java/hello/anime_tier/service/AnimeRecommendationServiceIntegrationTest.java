package hello.anime_tier.service;

import hello.anime_tier.domain.search.service.AnimeRecommendationService;
import hello.anime_tier.entity.AnimeEntity;
import hello.anime_tier.entity.AnimeTagMappingEntity;
import hello.anime_tier.entity.TagEntity;
import hello.anime_tier.repository.AnimeRepository;
import hello.anime_tier.repository.AnimeTagMappingRepository;
import hello.anime_tier.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AnimeRecommendationServiceIntegrationTest {

    @Autowired
    private AnimeRecommendationService animeRecommendationService;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AnimeTagMappingRepository animeTagMappingRepository;

    @BeforeEach
    void setUp() {
        // 1. 테스트용 태그 생성
        TagEntity actionTag = new TagEntity();
        actionTag.setTagId(1);
        actionTag.setTagName("Action");
        tagRepository.save(actionTag);

        TagEntity comedyTag = new TagEntity();
        comedyTag.setTagId(2);
        comedyTag.setTagName("Comedy");
        tagRepository.save(comedyTag);

        TagEntity fantasyTag = new TagEntity();
        fantasyTag.setTagId(3);
        fantasyTag.setTagName("Fantasy");
        tagRepository.save(fantasyTag);

        // 2. 테스트용 애니메이션 생성
        AnimeEntity anime1 = new AnimeEntity();
        anime1.setAnimeId(101);
        anime1.setTitleEn("Action Hero");
        anime1.setAverageScore(85);
        animeRepository.save(anime1);

        AnimeEntity anime2 = new AnimeEntity();
        anime2.setAnimeId(102);
        anime2.setTitleEn("Funny Fantasy");
        anime2.setAverageScore(90);
        animeRepository.save(anime2);

        // 3. 매핑 데이터 생성
        // Anime1: Action (Rank 80)
        AnimeTagMappingEntity mapping1 = new AnimeTagMappingEntity();
        mapping1.setAnime(anime1);
        mapping1.setTag(actionTag);
        mapping1.setTagRank(80);
        animeTagMappingRepository.save(mapping1);

        // Anime2: Comedy (Rank 90), Fantasy (Rank 70)
        AnimeTagMappingEntity mapping2 = new AnimeTagMappingEntity();
        mapping2.setAnime(anime2);
        mapping2.setTag(comedyTag);
        mapping2.setTagRank(90);
        animeTagMappingRepository.save(mapping2);

        AnimeTagMappingEntity mapping3 = new AnimeTagMappingEntity();
        mapping3.setAnime(anime2);
        mapping3.setTag(fantasyTag);
        mapping3.setTagRank(70);
        animeTagMappingRepository.save(mapping3);
    }

//    @Test
//    @DisplayName("태그 기반 애니메이션 추천 통합 테스트")
//    void recommendAnimesByTagsTest() {
//        // given
//        List<String> tags = List.of("Action", "Comedy", "Fantasy");
//
//        // when
//        List<AnimeEntity> recommendations = animeRecommendationService.recommendAnimesByTags(tags, tagResult.getPriorityTag());
//
//        // then
//        System.out.println("🎉 추천된 애니메이션 개수: " + recommendations.size());
//        recommendations.forEach(anime ->
//            System.out.println("📺 " + anime.getTitleEn() + " (평점: " + anime.getAverageScore() + ")")
//        );
//
//        assertThat(recommendations).isNotEmpty();
//        assertThat(recommendations).hasSize(2);
//
//        // 정렬 순서 검증:
//        // Anime2는 태그 2개 일치 (Comedy, Fantasy) -> 우선순위 높음
//        // Anime1은 태그 1개 일치 (Action) -> 우선순위 낮음
//        assertThat(recommendations.get(0).getTitleEn()).isEqualTo("Funny Fantasy");
//        assertThat(recommendations.get(1).getTitleEn()).isEqualTo("Action Hero");
//    }
    
//    @Test
//    @DisplayName("존재하지 않는 태그로 검색 시 빈 리스트 반환")
//    void recommendAnimesByUnknownTagsTest() {
//        // given
//        List<String> tags = List.of("UnknownTag123", "NotExistTag456");
//
//        // when
//        List<AnimeEntity> recommendations = animeRecommendationService.recommendAnimesByTags(tags, tagResult.getPriorityTag());
//
//        // then
//        assertThat(recommendations).isEmpty();
//    }
}