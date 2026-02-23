package hello.anime_tier.dto;

import hello.anime_tier.entity.AnimeEntity;
import lombok.Data;

@Data
public class AnimeResponseDto {
    private Integer animeId;
    private String titleEn;
    private String fullSynopsis;
    private Integer averageScore;
    private Integer popularity;
    private String coverImage;

    public static AnimeResponseDto fromEntity(AnimeEntity entity) {
        AnimeResponseDto dto = new AnimeResponseDto();
        dto.setAnimeId(entity.getAnimeId());
        dto.setTitleEn(entity.getTitleEn());
        dto.setFullSynopsis(entity.getFullSynopsis());
        dto.setAverageScore(entity.getAverageScore());
        dto.setPopularity(entity.getPopularity());
        dto.setCoverImage(entity.getCoverImage());
        return dto;
    }
}
