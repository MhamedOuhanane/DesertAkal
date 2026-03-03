package com.desertakal.desertakal.model.entity;

import com.desertakal.desertakal.model.enums.ReviewableType;
import com.desertakal.desertakal.model.interfaces.Reviewable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guides")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Guide extends User implements Reviewable {
    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "guide_languages",
            joinColumns = @JoinColumn(name = "guide_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private List<Language> languages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "guide", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    @Override
    public ReviewableType getReviewableType() {
        return ReviewableType.GUIDE;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
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
