package com.status_app.auth_service.service;

import com.status_app.auth_service.dto.AuthTokenDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface IAuthService {
    public Authentication getAuthContext();

    public AuthTokenDTO loginWithRefreshToken(String refreshToken);

    public AuthTokenDTO loginWithUsername(String username, String password, HttpServletResponse response);

    public AuthTokenDTO loginWithEmail(String email, String password, HttpServletResponse response);
}
