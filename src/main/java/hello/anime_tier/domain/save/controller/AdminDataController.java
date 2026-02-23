package hello.anime_tier.domain.save.controller;


import hello.anime_tier.domain.save.service.DataCleanService;
import hello.anime_tier.domain.save.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDataController {

    private final DataCleanService dataCleanService;
    private final EmbeddingService embeddingService;

    @GetMapping("/clean-data")
    public String cleanData() {
        dataCleanService.cleanAllSynopsis();
        return "모든 태그 제거 완료";
    }


    @GetMapping("/clean-data-Syno")
    public String cleanDataSynopsis() {
        dataCleanService.cleanAllSynopsisForChunks();
        return "모든 태그 제거 완료";
    }

    @GetMapping("/embedding/synopsis")
    public String embeddingSynopsis() {
        embeddingService.createEmbeddingsForSynopsis();
        return "문장 임베드화 진행중";
    }

    @GetMapping("/embedding/tags")
    public String embeddingTags() {
        embeddingService.createEmbeddingsForTags();
        return "태그 임베드화 진행중";
    }


}
