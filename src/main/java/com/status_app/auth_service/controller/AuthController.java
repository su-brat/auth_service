package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.ResponseTokenDTO;
import com.status_app.auth_service.entity.User;
import com.status_app.auth_service.jwt.JwtUtility;
import com.status_app.auth_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private UserService userService;

    @PostMapping("/verify")
    public ResponseEntity<?> authenticate() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Authenticated user: {}", username);
            User user = userService.getUser(username);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unable to authenticate: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtUtility.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing refresh token");
        }

        String username = jwtUtility.extractUsername(refreshToken);
        String newAccessToken = jwtUtility.generateToken(username);

        return ResponseEntity.ok(new ResponseTokenDTO(newAccessToken));
    }
}