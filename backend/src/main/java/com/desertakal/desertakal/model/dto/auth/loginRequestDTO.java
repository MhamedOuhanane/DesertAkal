package com.desertakal.desertakal.model.dto.login;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class loginRequestDTO {
    private String username;
    private String password;
}
