package com.desertakal.desertakal.model.entity;

import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.enums.ReviewableType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private String comment;

    @Column(name = "reviewable_id", nullable = false)
    private Long reviewableId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewable_type", nullable = false)
    private ReviewableType reviewableType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist")
    private Tourist tourist;

    @PrePersist
    public void prePersist(){
        if (uuid == null)
            uuid = UUID.randomUUID();
    }
}
