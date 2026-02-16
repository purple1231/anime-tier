package hello.anime_tier.repository;

import hello.anime_tier.entity.AnimeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AnimeRepository extends JpaRepository<AnimeEntity, Integer> {

    List<AnimeEntity> findByAverageScoreGreaterThanEqualOrderByPopularityDesc(Integer score);


    Page<AnimeEntity> findAll(Pageable pageable);


}



//메서드,설명
//save(T entity),새로운 데이터를 저장하거나 기존 데이터를 수정합니다.
//findById(ID id),ID(Primary Key)를 기준으로 데이터를 조회합니다.
//findAll(),테이블의 모든 데이터를 리스트로 반환합니다.
//deleteById(ID id),특정 ID의 데이터를 삭제합니다.
//count(),전체 데이터의 개수를 반환합니다.