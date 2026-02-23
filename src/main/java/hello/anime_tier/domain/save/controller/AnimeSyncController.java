package hello.anime_tier.domain.save.controller;


import hello.anime_tier.domain.save.service.AnimeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AnimeSyncController {

    private final AnimeSyncService animeSyncService;


    @GetMapping("/sync-anime")
    public String syncAnime(@RequestParam(defaultValue = "1") int pages) {

        animeSyncService.syncData(pages);
        return "✅ " + pages + "페이지 분량의 동기화가 백그라운드에서 시작되었습니다. 로그를 확인하세요.";
    }
}
