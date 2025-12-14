package com.desertakal.desertakal.model.dto.auth;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class loginDTO {
    private UUID uuid;
    private String username;
    private String fullName;
    private String role;
    private String accessToken;
    private String refreshToken;
}
