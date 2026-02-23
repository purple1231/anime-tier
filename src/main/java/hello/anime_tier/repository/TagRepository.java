package hello.anime_tier.repository;

import hello.anime_tier.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Integer> {


    Optional<TagEntity> findByTagName(String tagName);


    @Query(value = """
        SELECT tag_name
        FROM tags
        WHERE tag_embedding IS NOT NULL
        ORDER BY tag_embedding <-> CAST(:vec AS vector)
        LIMIT :k
        """, nativeQuery = true)
    List<String> findTopKTagNamesByVector(@Param("vec") String vec,
                                          @Param("k") int k);



}
