package com.desertakal.desertakal.model.dto.auth;

import com.desertakal.desertakal.model.enums.OauthProvider;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class loginRequestDTO {
    private String username;
    private String password;

    private OauthProvider provider;
    private String providerId;
}
