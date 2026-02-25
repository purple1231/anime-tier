package hello.anime_tier.domain.search.service;

import hello.anime_tier.entity.AnimeEntity;
import hello.anime_tier.repository.AnimeRepository;
import hello.anime_tier.repository.AnimeTagMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimeRecommendationService {

    private final AnimeTagMappingRepository animeTagMappingRepository;
    private final AnimeRepository animeRepository;
    private final StorySearchService storySearchService;

    @Transactional(readOnly = true)
    public List<AnimeEntity> recommendAnimesByTags(List<String> tags, String priorityTag, String prompt) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        //0. 일단 스토리 챙기자
        java.util.Map<Integer, Integer> rankmap = storySearchService.getStoryTop20RankMap(prompt);

        // 1. 태그와 일치하는 애니메이션 ID 조회 (일치 개수 내림차순, 태그 점수 합 내림차순, 평점 내림차순 정렬)
        // Pageable을 사용하여 상위 30개만 조회
        Pageable pageable = PageRequest.of(0, 30);
        List<Integer> animeIds = animeTagMappingRepository.findAnimeIdsByTags(tags, priorityTag,2L, pageable);
        animeIds = rerankByStoryBoost(animeIds, rankmap); // 1.5 스토리 랭킹에 따라 리랭크

        if (animeIds.isEmpty()) {
            return List.of();
        }

        // 2. ID로 애니메이션 엔티티 조회
        List<AnimeEntity> animes = animeRepository.findAllById(animeIds);

        // 3. 조회된 엔티티를 ID 기반 Map으로 변환 (빠른 조회를 위해)
        Map<Integer, AnimeEntity> animeMap = animes.stream()
                .collect(Collectors.toMap(AnimeEntity::getAnimeId, Function.identity())); // AnimeEntity::getAnimeId (Key) Function.identity() (Value)

        // 4. 원래 ID 순서(추천 순위)대로 정렬하여 반환
        return animeIds.stream()
                .map(animeMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }




    private int storyBonus(int rank) {
        if (rank <= 5) return 120;   // 1~5 많이
        if (rank <= 10) return 70;   // 6~10 조금 많이
        if (rank <= 15) return 40;   // 11~15 중간
        if (rank <= 20) return 20;   // 16~20 조금
        return 0;
    }

    private List<Integer> rerankByStoryBoost(List<Integer> tagAnimeIds,
                                             Map<Integer, Integer> storyRankMap) {

        int base = 10000; // 태그 순위 기본점 (충분히 크게)
        List<ScoredId> scored = new java.util.ArrayList<>(tagAnimeIds.size());

        for (int i = 0; i < tagAnimeIds.size(); i++) {
            int animeId = tagAnimeIds.get(i);

            // 태그 쿼리에서 이미 정렬돼 있으니까: 앞쪽일수록 점수 높게
            int score = base - i;

            Integer storyRank = storyRankMap.get(animeId);
            if (storyRank != null) {
                score += storyBonus(storyRank);
            }

            scored.add(new ScoredId(animeId, score));
        }

        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        return scored.stream().map(s -> s.animeId).toList();
    }

    private record ScoredId(int animeId, int score) {}

}


//📋 전체 과정 요약 (Fact Check)
//아이디 조회 (animeIds): 정렬 조건(내림차순 등)이 적용된 **'완벽한 순서'**를 가진 정답지입니다.
//
//엔티티 조회 (findAllById): DB에 "이 아이디들 다 가져와!"라고 시키면, DB는 성능을 위해 자기 마음대로(보통 PK 순서로) 데이터를 던져줍니다. (여기서 순서가 깨짐 💔)
//
//Map 변환 (animeMap): 깨진 순서의 리스트를 그대로 쓰면 찾기가 힘드니, 아이디만 주면 바로 객체를 뱉어내는 **'고속 보관함'**에 넣어두는 것입니다.
//
//최종 재정렬: 원래의 **'완벽한 순서(Step 1)'**를 하나씩 읽으면서, 보관함(Map)에서 해당 객체를 꺼내 리스트를 새로 만듭니다. (순서 복구 완료! ✨)



