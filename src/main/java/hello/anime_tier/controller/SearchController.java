package hello.anime_tier.controller;


import hello.anime_tier.service.StorySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final StorySearchService searchService;

    @PostMapping
    public List<String> search(@RequestBody SearchRequest request) { // @RequestParam에서 변경
        return searchService.searchAnime(request.getQuery());
    }


    @lombok.Data
    public static class SearchRequest {
        private String query;
    }

}
