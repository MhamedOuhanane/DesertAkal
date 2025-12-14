package com.desertakal.desertakal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "map_lat", nullable = false, precision = 10, scale = 6)
    private BigDecimal map_lat;

    @Column(name = "map_lng", nullable = false, precision = 10, scale = 6)
    private BigDecimal map_lng;

    @Column(nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private List<CityTour> cityTours = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        if (uuid == null)
            uuid = UUID.randomUUID();
    }
}
