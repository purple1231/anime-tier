package hello.anime_tier.dto;


import lombok.Data;
import java.util.List;

public class AniListDto {

    @Data
    public static class AniListResponse {
        private DataContainer data;

        @Data
        public static class DataContainer {
            private PageContainer Page;
        }

        @Data
        public static class PageContainer {
            private PageInfo pageInfo;
            private List<Media> media;
        }

        @Data
        public static class PageInfo {
            private int total;
            private boolean hasNextPage;
        }

        /**
         * [Media]
         * 애니메이션 1개당 상세 정보
         */
        @Data
        public static class Media {
            private int id;
            private Title title;
            private String description;     // 줄거리 (HTML 태그 포함됨)
            private Integer averageScore;   // 평점
            private int popularity;         // 인기도 (필터링용)
            private CoverImage coverImage;  // 포스터 이미지
            private List<Tag> tags;         // 태그 리스트 (Rank 포함)
        }

        @Data
        public static class Title {
            private String english;
            private String romaji;
        }

        @Data
        public static class CoverImage {
            private String large;
        }

        /**
         * [Tag]
         * 애니메이션에 달린 태그와 연관도(Rank)
         */
        @Data
        public static class Tag {
            private int id;
            private String name;
            private int rank; // 0~100 사이의 가중치
        }
    }
}


/**
 * [data 어노테이션]
 * Getter, Setter, toString, equals, 생성자 자동생성 기능 가짐
 */