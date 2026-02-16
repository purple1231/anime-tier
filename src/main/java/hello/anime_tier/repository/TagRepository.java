package hello.anime_tier.repository;

import hello.anime_tier.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Integer> {

    Optional<TagEntity> findByTagName(String tagName);
}
