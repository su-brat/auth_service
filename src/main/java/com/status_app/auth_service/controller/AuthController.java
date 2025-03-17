package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.LoginRequestDTO;
import com.status_app.auth_service.dto.ResponseTokenDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.jwt.JwtUtility;
import com.status_app.auth_service.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/verify")
    public ResponseEntity<UserDTO> authenticate() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Authenticated user: {}", username);
            UserDTO user = userService.getUser(username);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unable to authenticate: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseTokenDTO> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtUtility.validateToken(refreshToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtility.extractUsername(refreshToken);
        String newAccessToken = jwtUtility.generateToken(username);

        return ResponseEntity.ok(new ResponseTokenDTO(newAccessToken));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseTokenDTO> login(@RequestBody LoginRequestDTO requestBody, HttpServletResponse response) {
        try {
            String username = requestBody.getUsername();
            String password = requestBody.getPassword();
            if (username == null) {
                String email = requestBody.getEmail();
                if (email == null) {
                    throw new BadRequestException("Bad credentials");
                }
                UserDTO user = userService.getUserByEmail(email);
                if (user == null) {
                    throw new BadCredentialsException("Bad credentials");
                }
                username = user.getUsername();
            }
            log.info("Logging in user: {}", username);
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            long currentTimeMillis = System.currentTimeMillis();
            String accessToken = jwtUtility.generateToken(username);
            String refreshToken = jwtUtility.generateRefreshToken(username);
            long expirationTimeMillis = jwtUtility.extractExpiration(refreshToken).getTime();
            long expirationTimeSecs = (expirationTimeMillis - currentTimeMillis) / 1000;
            log.info("Authenticated user: {}, and generated access and refresh token", username);
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(expirationTimeSecs) // expiration time of the refresh token (7 days)
                    .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());
            return new ResponseEntity<>(new ResponseTokenDTO(accessToken), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}