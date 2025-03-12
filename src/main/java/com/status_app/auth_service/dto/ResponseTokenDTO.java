package com.status_app.auth_service.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class ResponseTokenDTO {

    @NonNull
    private String token;
}
