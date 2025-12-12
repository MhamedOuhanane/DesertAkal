package com.desertakal.desertakal.model.entity;

import com.desertakal.desertakal.model.enums.ReviewableType;
import com.desertakal.desertakal.model.interfaces.Reviewable;
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
@Table(name = "tours")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tour implements Reviewable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    protected UUID uuid;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private String image;

    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    protected BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "tour")
    private List<CityTours> cityTours = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        if (uuid == null)
            uuid = UUID.randomUUID();
    }

    @Override
    public ReviewableType getReviewableType() {
        return ReviewableType.TOUR;
    }

    @Override
    public String getDisplayName() {
        return title;
    }

    @Override
    public void updateAverageRating(BigDecimal newAverage) {
        rating = newAverage;
    }

    @Override
    public void incrementReviewCount() {
        reviewCount = (reviewCount == null ? 0 : reviewCount) + 1;
    }

    @Override
    public void decrementReviewCount() {
        reviewCount = (reviewCount == null || reviewCount <= 0)
                ? 0
                : reviewCount - 1;
    }
}
