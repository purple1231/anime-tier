package hello.anime_tier.service;

import hello.anime_tier.dto.AniListDto;
import hello.anime_tier.entity.AnimeEntity;
import hello.anime_tier.entity.AnimeTagMappingEntity;
import hello.anime_tier.entity.SynopsisChunkEntity;
import hello.anime_tier.entity.TagEntity;
import hello.anime_tier.repository.AnimeRepository;
import hello.anime_tier.repository.AnimeTagMappingRepository;
import hello.anime_tier.repository.SynopsisChunkRepository;
import hello.anime_tier.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AniList API와 연동하여 애니메이션 데이터를 동기화하는 서비스입니다.
 * 외부 API로부터 데이터를 가져와 내부 DB에 저장하는 역할을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor // 생성자 주입을 위한 Lombok 어노테이션
public class AnimeSyncService {

    // AniList GraphQL API 쿼리
    // 애니메이션(ANIME) 타입, 점수 내림차순 정렬, 평균 점수 80점 이상인 데이터 조회
    private static final String ANILIST_QUERY =
            "query ($page: Int, $perPage: Int) { " +
                    "  Page(page: $page, perPage: $perPage) { " +
                    "    pageInfo { " +
                    "      total " +
                    "      hasNextPage " +
                    "    } " +
                    "    media(type: ANIME, sort: SCORE_DESC, averageScore_greater: 80) { " + // 필터 조건: 애니메이션, 점수순 정렬, 80점 이상
                    "      id " +
                    "      title { english romaji } " + // 제목 (영어, 로마지)
                    "      description " + // 줄거리
                    "      averageScore " + // 평균 점수
                    "      popularity " + // 인기도
                    "      coverImage { large } " + // 커버 이미지 (큰 사이즈)
                    "      tags { id name rank } " + // 태그 정보
                    "    } " +
                    "  } " +
                    "}";


    private final AnimeSaveService animeSaveService;
    private final RestTemplate restTemplate;

    private static final String URL = "https://graphql.anilist.co";

    @Async
    public void syncData(int totalPages){
        for (int page = 1; page <= totalPages; page++){
            log.info("현재 수집 중인 페이지: {}", page);

            try {
                AniListDto.AniListResponse response = fetchPage(page);
                if (response == null) break;

                List<AniListDto.AniListResponse.Media> mediaList = response.getData().getPage().getMedia();

                for (var media : mediaList) {
                    animeSaveService.processMedia(media);
                }

                Thread.sleep(1500);

            }catch (Exception e){
                log.error("❌ 페이지 {} 수집 중 에러 발생: {}", page, e.getMessage());
            }
        }
    }

    private AniListDto.AniListResponse fetchPage(int page){
        Map<String, Object> variables = new HashMap<>();
        variables.put("page",page);
        variables.put("perPage",50);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", ANILIST_QUERY);
        requestBody.put("variables", variables);

        return restTemplate.postForObject(URL, requestBody, AniListDto.AniListResponse.class);

    }
}
