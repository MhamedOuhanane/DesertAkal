package com.desertakal.desertakal.model.dto.auth;

import com.desertakal.desertakal.model.enums.OauthProvider;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegisterDTO {
    private String firstName;
    private String lastName;

    private String username;
    private String email;

    private String password;
    private String confirmPassword;

    private Long roleUuid;

    private String nationality;
    private String language;

    private OauthProvider oauthProvider;
    private String providerId;
}
