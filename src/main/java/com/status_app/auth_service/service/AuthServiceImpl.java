package com.status_app.auth_service.service;

import com.status_app.auth_service.dto.AuthTokenDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.jwt.JwtUtility;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements IAuthService{

    private final JwtUtility jwtUtility;

    private final AuthenticationManager authenticationManager;

    private final IUserService userService;

    @Autowired
    public AuthServiceImpl(JwtUtility jwtUtility, AuthenticationManager authenticationManager, IUserService userService) {
        this.jwtUtility = jwtUtility;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @Override
    public Authentication getAuthContext() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public AuthTokenDTO loginWithRefreshToken(String refreshToken) {
        try {
            if (jwtUtility.validateToken(refreshToken)) {
                String username = jwtUtility.extractUsername(refreshToken);
                String newAccessToken = jwtUtility.generateToken(username);
                return new AuthTokenDTO(newAccessToken);
            }
            throw new BadCredentialsException("Invalid refresh token");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public AuthTokenDTO loginWithUsername(String username, String password, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            String accessToken = jwtUtility.generateToken(username);
            String refreshToken = jwtUtility.generateRefreshToken(username);
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(jwtUtility.getRefreshTokenExpirySeconds()) // expiration time of the refresh token (7 days)
                    .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());
            return new AuthTokenDTO(accessToken);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public AuthTokenDTO loginWithEmail(String email, String password, HttpServletResponse response) {
        try {
            UserDTO user = userService.getUserByEmail(email);
            return loginWithUsername(user.getUsername(), password, response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
