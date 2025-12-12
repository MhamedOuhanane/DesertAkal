package com.desertakal.desertakal.model.entity;

import com.desertakal.desertakal.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    protected UUID uuid;

    @CreationTimestamp
    @Column(nullable = false)
    protected LocalDateTime date;

    @Column(name = "start_date", nullable = false)
    protected LocalDateTime startDate;

    @Column(name = "number_people", nullable = false)
    protected Integer numberPeople;

    @Column(nullable = false, precision = 15, scale = 2)
    protected BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Column(name = "pdf_url", nullable = false)
    private String pdfUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private Tour guide;

    @ManyToOne
    @JoinColumn(name = "tourist_id")
    private Tour tourist;

    @PrePersist
    public void prePersist(){
        if (uuid == null)
            uuid = UUID.randomUUID();
    }
}
