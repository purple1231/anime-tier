package hello.anime_tier.domain.save.service;


import hello.anime_tier.repository.AnimeRepository;
import hello.anime_tier.repository.SynopsisChunkRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataCleanService {

    private final AnimeRepository animeRepository;
    private final SynopsisChunkRepository synopsisChunkRepository;

    @Transactional // 자동 커밋/롤백
    public void cleanAllSynopsis() {
        // 모든 애니메이션을 가져와서 HTML 태그 제거
        animeRepository.findAll().forEach(anime -> {
            String raw = anime.getFullSynopsis();
            if (raw != null) {
                // <i>Kingdom</i> -> Kingdom 변환
                String clean = Jsoup.parse(raw).text();
                anime.setFullSynopsis(clean);
            }
        });
        // 트랜잭션 종료 시 일괄 업데이트(Dirty Checking)
    }


    @Transactional
    public void cleanAllSynopsisForChunks() {
        // 모든 애니메이션을 가져와서 HTML 태그 제거 ( TABLE anime_synopsis_chunks 용 )
        synopsisChunkRepository.findAll().forEach(animeSynopsis -> {
            String raw = animeSynopsis.getChunkText();
            if (raw != null) {
                // <i>Kingdom</i> -> Kingdom 변환
                String clean = Jsoup.parse(raw).text();
                animeSynopsis.setChunkText(clean);
            }
        });
    }

}
