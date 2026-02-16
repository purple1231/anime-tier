package hello.anime_tier.dto;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

class AniListDtoTest {

    @Test
    void aniListApiTest() {
        // 1. 통신 도구 준비
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://graphql.anilist.co";

        // 2. GraphQL 쿼리 (가장 심플하게 딱 필요한 것만)
        String query =
                "query ($id: Int) { " +
                        "  Page(page: 1, perPage: 1) { " +
                        "    media(id: $id, type: ANIME) { " +
                        "      id " +
                        "      title { english romaji } " +
                        "      description " +
                        "      tags { id name rank } " +
                        "    } " +
                        "  } " +
                        "}";

        // 3. 변수 설정 (최애의 아이 ID: 16498)
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", 16498);

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("variables", variables);

        // 4. 헤더 설정 (JSON으로 보낸다고 알려줌)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 5. API 찌르기! (결과를 사용자님의 AniListDto.AniListResponse에 담기)
        try {
            ResponseEntity<AniListDto.AniListResponse> response =
                    restTemplate.postForEntity(url, request, AniListDto.AniListResponse.class);

            // 6. 콘솔에 출력해서 눈으로 확인하기 👀
            if (response.getBody() != null) {
                var media = response.getBody().getData().getPage().getMedia().get(0);

                System.out.println("========================================");
                System.out.println("📺 애니 제목: " + media.getTitle().getEnglish());
                System.out.println("🆔 애니 ID: " + media.getId());
                System.out.println("📝 줄거리 요약: " + media.getDescription().substring(0, 50) + "...");
                System.out.println("🏷️ 첫 번째 태그: " + media.getTags().get(0).getName());
                System.out.println("========================================");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}