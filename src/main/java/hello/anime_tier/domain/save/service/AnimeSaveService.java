package hello.anime_tier.domain.save.service;

import com.pgvector.PGvector;
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
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor // 생성자 자동완성 final
public class AnimeSaveService {

    private final AnimeRepository animeRepository;
    private final TagRepository tagRepository;
    private final AnimeTagMappingRepository mappingRepository;
    private final SynopsisChunkRepository chunkRepository;

    private final TransformersEmbeddingModel embeddingModel;

    @Transactional // 중간에 하나라도 실패하면 rollback 기능
    public void processMedia(AniListDto.AniListResponse.Media media){
        //상속 관계나 같은 패키지 내에서는 접근 가능하면서 트랜잭션 기능을 살릴 수 있는" protected를 선택한 경우가 많습니다.



        // 1. amimeEntity 저장
        AnimeEntity anime = new AnimeEntity();
        anime.setAnimeId(media.getId());
        anime.setTitleEn(media.getTitle().getEnglish() != null ? media.getTitle().getEnglish() : media.getTitle().getRomaji());
        anime.setFullSynopsis(media.getDescription());
        anime.setAverageScore(media.getAverageScore());
        anime.setPopularity(media.getPopularity());
        anime.setCoverImage(media.getCoverImage().getLarge());
        animeRepository.save(anime);

        //2.Tag 및 Mapping 처리 (N:M)
        for(var tagDto : media.getTags()){
            TagEntity tag = tagRepository.findById(tagDto.getId())
                    .orElseGet(() -> {
                        TagEntity newTag = new TagEntity();
                        newTag.setTagId(tagDto.getId());
                        newTag.setTagName(tagDto.getName());
                        float[] tagVector = embeddingModel.embed(tagDto.getName());
                        newTag.setTagEmbedding(tagVector);
                        return tagRepository.save(newTag);
                    });

            AnimeTagMappingEntity mapping = new AnimeTagMappingEntity();
            mapping.setAnime(anime);
            mapping.setTag(tag);
            mapping.setTagRank(tagDto.getRank());
            mappingRepository.save(mapping);

        }

        // 3. 줄거리 텍스트 청크화 (2주차 임베딩 준비 단계)
        SynopsisChunkEntity chunk = new SynopsisChunkEntity();
        chunk.setAnime(anime);
        chunk.setChunkText(media.getDescription()); // 현재는 통째로 저장, 추후 분할 로직 추가 가능
        float[] embedding = embeddingModel.embed(media.getDescription());
        chunk.setEmbedding(embedding);

        chunkRepository.save(chunk);



    }
}
