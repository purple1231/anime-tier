package hello.anime_tier.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tags")
@Data
public class TagEntity {
    @Id
    @Column(name = "tag_id")
    private Integer tagId;

    @Column(unique = true)
    private String tagName;

    @Lob
    @Column(name = "tag_embedding", columnDefinition = "MEDIUMBLOB")
    private byte[] tagEmbedding;
}