package hello.anime_tier.entity;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name="embedding", columnDefinition="vector(384)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @org.hibernate.annotations.Array(length = 384)
    private float[] embedding;
}