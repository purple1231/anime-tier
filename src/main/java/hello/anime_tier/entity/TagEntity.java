package hello.anime_tier.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import com.pgvector.PGvector;

@Entity
@Table(name = "tags")
@Data
public class TagEntity {
    @Id
    @Column(name = "tag_id")
    private Integer tagId;

    @Column(unique = true)
    private String tagName;

    @Column(name="tag_embedding", columnDefinition="vector(384)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @org.hibernate.annotations.Array(length = 384)
    private float[] tagEmbedding;
}