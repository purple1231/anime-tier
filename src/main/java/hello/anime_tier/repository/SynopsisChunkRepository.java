package hello.anime_tier.repository;

import hello.anime_tier.entity.AnimeEntity;
import hello.anime_tier.entity.SynopsisChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SynopsisChunkRepository extends JpaRepository<SynopsisChunkEntity, Long> {


    // 특정 애니메이션에 속한 모든 텍스트 청크 가져오기
    List<SynopsisChunkEntity> findByAnime(AnimeEntity anime);


    //임베딩 값 없는것만 가져와서 나중에 임베딩 처리 할거임
    List<SynopsisChunkEntity> findByEmbeddingIsNull();


    //이름 반환
    @Query(value = """
        SELECT a.title_en
        FROM anime_synopsis_chunks c
        JOIN anime a ON c.anime_id = a.anime_id
        WHERE c.embedding IS NOT NULL
        ORDER BY c.embedding <-> CAST(:vec AS vector)
        LIMIT :k
        """, nativeQuery = true)
    List<String> findTopKAnimeTitlesByVector(@Param("vec") String vec,
                                             @Param("k") int k);


    //아이디 반환
    @Query(value = """
    SELECT c.anime_id
    FROM anime_synopsis_chunks c
    WHERE c.embedding IS NOT NULL
    ORDER BY c.embedding <-> CAST(:vec AS vector)
    LIMIT :k
    """, nativeQuery = true)
    List<Integer> findTopKAnimeIdsByVector(@Param("vec") String vec,
                                           @Param("k") int k);
}
