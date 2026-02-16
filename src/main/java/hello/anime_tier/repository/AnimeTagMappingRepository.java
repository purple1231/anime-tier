package hello.anime_tier.repository;

import hello.anime_tier.entity.AnimeTagMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeTagMappingRepository extends JpaRepository<AnimeTagMappingEntity, Long> {
}
