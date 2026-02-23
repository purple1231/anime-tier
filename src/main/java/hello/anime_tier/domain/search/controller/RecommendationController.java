package hello.anime_tier.domain.search.controller;


import hello.anime_tier.domain.search.service.AnimeRecommendationService;
import hello.anime_tier.dto.AnimeResponseDto;
import hello.anime_tier.dto.RecommendationResponseDto;
import hello.anime_tier.dto.TagSearchResponseDto;
import hello.anime_tier.entity.AnimeEntity;
import hello.anime_tier.domain.search.service.TagSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final TagSearchService tagSearchService;
    private final AnimeRecommendationService animeRecommendationService;


    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponseDto> recommendation(@RequestBody String prompt) {
        // 1. 사용자 입력(프롬프트)에서 핵심 태그 추출 (임베딩 -> DB검색 -> LLM 필터링)
        // 이제 후보 태그와 최종 태그를 모두 포함한 DTO를 반환받음
        TagSearchResponseDto tagResult = tagSearchService.extractTopFinalTags(prompt);

        log.info("선택된 태그: {}", tagResult.getFinalTags());

        // 2. 추출된 최종 태그를 기반으로 애니메이션 추천 (태그 일치도 & 평점 순)
        List<AnimeEntity> recommendedAnimes = animeRecommendationService.recommendAnimesByTags(tagResult.getFinalTags(), tagResult.getPriorityTag());

        log.info("추천된 애니메이션 개수: {}", recommendedAnimes.size());

        // 3. 엔티티를 DTO로 변환하여 순환 참조 방지 및 필요한 데이터만 반환
        List<AnimeResponseDto> animeDtos = recommendedAnimes.stream()
                .map(AnimeResponseDto::fromEntity)
                .collect(Collectors.toList());

        // 4. 후보 태그, 선택된 태그, 우선순위 태그, 애니메이션 리스트를 함께 반환
        return ResponseEntity.ok(new RecommendationResponseDto(
                tagResult.getCandidateTags(),
                tagResult.getFinalTags(),
                tagResult.getPriorityTag(),
                animeDtos
        ));
    }
}
