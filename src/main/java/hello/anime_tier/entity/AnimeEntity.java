package hello.anime_tier.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;



@Entity
@Table(name = "anime")
@Data
public class AnimeEntity {

    @Id
    @Column(name = "anime_id")
    private Integer animeId;

    private String TitleEn;

    @Column(columnDefinition = "TEXT")
    private String fullSynopsis;

    private Integer averageScore;
    private Integer popularity;
    private String coverImage;

    // 연관관계 설정 (참고용)
    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL)
    private List<SynopsisChunkEntity> chunks;

}