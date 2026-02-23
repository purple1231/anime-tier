package hello.anime_tier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResponseDto {
    private List<String> candidateTags;
    private List<String> selectedTags;
    private String priorityTag;
    private List<AnimeResponseDto> animes;
}
