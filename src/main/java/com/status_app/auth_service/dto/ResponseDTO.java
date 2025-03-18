package com.status_app.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseDTO<T> {
    private T responseObj;
    private String message;
    private String status;

    public String toString() {
        return "GenericResponseDTO{" +
                "responseObj=" + responseObj +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
