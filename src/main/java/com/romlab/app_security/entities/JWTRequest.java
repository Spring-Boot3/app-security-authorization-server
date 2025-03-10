package com.romlab.app_security.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JWTRequest {

    private String username;
    private String password;

}
