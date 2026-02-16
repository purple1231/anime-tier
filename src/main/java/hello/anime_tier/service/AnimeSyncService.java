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


@Slf4j
@Service
@RequiredArgsConstructor // 생성자 자동완성 final
public class AnimeSyncService {

    private static final String ANILIST_QUERY =
            "query ($page: Int, $perPage: Int) { " +
                    "  Page(page: $page, perPage: $perPage) { " +
                    "    pageInfo { " +
                    "      total " +
                    "      hasNextPage " +
                    "    } " +
                    "    media(type: ANIME, sort: SCORE_DESC, averageScore_greater: 80) { " + // 여기에 추가!
                    "      id " +
                    "      title { english romaji } " +
                    "      description " +
                    "      averageScore " +
                    "      popularity " +
                    "      coverImage { large } " +
                    "      tags { id name rank } " +
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
