package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.LoginRequestDTO;
import com.status_app.auth_service.dto.ResponseTokenDTO;
import com.status_app.auth_service.jwt.JwtUtility;
import com.status_app.auth_service.entity.User;
import com.status_app.auth_service.service.UserDetailsServiceImpl;
import com.status_app.auth_service.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtility jwtUtility;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO requestBody, HttpServletResponse response) {
        try {
            String username = requestBody.getUsername();
            String password = requestBody.getPassword();
            if (username == null) {
                String email = requestBody.getEmail();
                if (email == null) {
                    throw new UsernameNotFoundException("Username or email is required");
                }
                User user = userService.getUserByEmail(email);
                if (user == null) {
                    throw new UsernameNotFoundException("User not found with email: " + email);
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/new")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
        } catch (Exception e)   {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
