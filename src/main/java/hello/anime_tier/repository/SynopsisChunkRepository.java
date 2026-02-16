package hello.anime_tier.repository;

import hello.anime_tier.entity.AnimeEntity;
import hello.anime_tier.entity.SynopsisChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SynopsisChunkRepository extends JpaRepository<SynopsisChunkEntity, Long> {


    // 특정 애니메이션에 속한 모든 텍스트 청크 가져오기
    List<SynopsisChunkEntity> findByAnime(AnimeEntity anime);


    //임베딩 값 없는것만 가져와서 나중에 임베딩 처리 할거임
    List<SynopsisChunkEntity> findByEmbeddingIsNull();
}
