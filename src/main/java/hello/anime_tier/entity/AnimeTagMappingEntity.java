package hello.anime_tier.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "anime_tag_mapping")
@Data
public class AnimeTagMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private AnimeEntity anime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private TagEntity tag;

    private Integer tagRank;
}