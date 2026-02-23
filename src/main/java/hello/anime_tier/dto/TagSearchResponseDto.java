package hello.anime_tier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class TagSearchResponseDto {
    private List<String> candidateTags;
    private List<String> finalTags;
    private String priorityTag; // [] 핵심태그임
}
