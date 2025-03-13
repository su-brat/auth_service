package com.status_app.auth_service.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class LoginRequestDTO {
    private String username;

    private String email;

    @NonNull
    private String password;
}
