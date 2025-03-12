package com.status_app.auth_service.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class LoginRequestDTO {
    @NonNull
    private String username;

    @NonNull
    private String password;
}
