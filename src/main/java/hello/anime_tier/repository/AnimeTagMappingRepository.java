package hello.anime_tier.repository;

import hello.anime_tier.entity.AnimeTagMappingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimeTagMappingRepository extends JpaRepository<AnimeTagMappingEntity, Long> {

    @Query("""
        SELECT m.anime.animeId
        FROM AnimeTagMappingEntity m
        WHERE m.tag.tagName IN :tags
        GROUP BY m.anime.animeId
        HAVING COUNT(m.tag.tagId) >= :minMatch
        ORDER BY
          MAX(CASE WHEN m.tag.tagName = :priorityTag THEN 1 ELSE 0 END) DESC,
          COUNT(m.tag.tagId) DESC,
          (SUM(COALESCE(m.tagRank, 0)) * 0.8 + MAX(COALESCE(m.anime.popularity, 0)) * 0.2) DESC,
          m.anime.animeId ASC
        """)
    List<Integer> findAnimeIdsByTags(
            @Param("tags") List<String> tags,
            @Param("priorityTag") String priorityTag,
            @Param("minMatch") long minMatch,
            Pageable pageable
    );
}