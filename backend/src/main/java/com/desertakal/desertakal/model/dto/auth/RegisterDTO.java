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
    protected String firstName;
    protected String lastName;

    protected String username;
    protected String email;

    protected String password;
    protected String confirmPassword;

    protected Long roleUuid;

    protected String nationality;
    protected String language;

    protected OauthProvider oauthProvider;
    protected String providerId;
}
