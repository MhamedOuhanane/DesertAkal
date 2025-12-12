package com.desertakal.desertakal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "articles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    protected UUID uuid;

    @Column(nullable = false)
    private String content;

    @Column(name = "cover_image", nullable = false)
    private String coverImage;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @Column(name = "reaction_count", nullable = false)
    private Integer reactionCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        if (uuid == null)
            uuid = UUID.randomUUID();
    }

    public void incrementReactionCount() {
        reactionCount = (reactionCount == null ? 0 : reactionCount) + 1;
    }

    public void incrementCommentCount() {
        commentCount = (commentCount == null ? 0 : commentCount) + 1;
    }

    public void decrementReactionCount() {
        reactionCount = (reactionCount == null || reactionCount <= 0)
                ? 0
                : reactionCount - 1;
    }

    public void decrementCommentCount() {
        commentCount = (commentCount == null || commentCount <= 0)
                ? 0
                : commentCount - 1;
    }
}
