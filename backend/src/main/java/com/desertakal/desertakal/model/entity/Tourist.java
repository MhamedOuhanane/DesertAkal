package com.desertakal.desertakal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tourists")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tourist extends User {
    @Column(name = "avatar_url")
    private String avatarUrl;

    private String nationality;
    private String language;

    @Builder.Default
    @OneToMany(mappedBy = "tourist", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tourist", fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
