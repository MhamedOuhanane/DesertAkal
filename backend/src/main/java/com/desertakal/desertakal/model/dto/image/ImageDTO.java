package com.desertakal.desertakal.model.dto.image;

import com.desertakal.desertakal.model.entity.City;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ImageDTO {
    private UUID uuid;
    private String image;
    private Boolean isCover;
    private LocalDateTime createdAt;
    private String cityName;
}
