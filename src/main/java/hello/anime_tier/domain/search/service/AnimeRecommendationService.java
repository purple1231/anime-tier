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

    @Transactional(readOnly = true)
    public List<AnimeEntity> recommendAnimesByTags(List<String> tags, String priorityTag) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        // 1. 태그와 일치하는 애니메이션 ID 조회 (일치 개수 내림차순, 태그 점수 합 내림차순, 평점 내림차순 정렬)
        // Pageable을 사용하여 상위 30개만 조회
        Pageable pageable = PageRequest.of(0, 30);
        List<Integer> animeIds = animeTagMappingRepository.findAnimeIdsByTags(tags, priorityTag,2L, pageable);

        if (animeIds.isEmpty()) {
            return List.of();
        }

        // 2. ID로 애니메이션 엔티티 조회
        List<AnimeEntity> animes = animeRepository.findAllById(animeIds);

        // 3. 조회된 엔티티를 ID 기반 Map으로 변환 (빠른 조회를 위해)
        Map<Integer, AnimeEntity> animeMap = animes.stream()
                .collect(Collectors.toMap(AnimeEntity::getAnimeId, Function.identity()));

        // 4. 원래 ID 순서(추천 순위)대로 정렬하여 반환
        return animeIds.stream()
                .map(animeMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}
