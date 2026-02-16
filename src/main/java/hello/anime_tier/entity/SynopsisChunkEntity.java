package hello.anime_tier.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "anime_synopsis_chunks")
@Data
public class SynopsisChunkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chunkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id")
    private AnimeEntity anime;

    @Column(columnDefinition = "TEXT")
    private String chunkText;

    @Column(name = "embedding", columnDefinition = "LONGBLOB")
    private byte[] embedding; // 2주차 벡터 저장용
}