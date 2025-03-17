package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.LoginRequestDTO;
import com.status_app.auth_service.dto.ResponseTokenDTO;
import com.status_app.auth_service.jwt.JwtUtility;
import com.status_app.auth_service.entity.Role;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

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
            User user = null;
            if (username == null) {
                String email = requestBody.getEmail();
                if (email == null) {
                    throw new UsernameNotFoundException("Username or email is required");
                }
                user = userService.getUserByEmail(email);
                if (user == null) {
                    throw new UsernameNotFoundException("User not found with email: " + email);
                }
                username = user.getUsername();
            }
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            if (user == null) {
                user = userService.getUser(username);
            }
            if (user == null || !user.getRoles().contains(Role.ADMIN)) {
                throw new UsernameNotFoundException("Admin username not found");
            }
            long currentTimeMillis = System.currentTimeMillis();
            String accessToken = jwtUtility.generateToken(username);
            String refreshToken = jwtUtility.generateRefreshToken(username);
            long expirationTimeMillis = jwtUtility.extractExpiration(refreshToken).getTime();
            long expirationTimeSecs = (expirationTimeMillis - currentTimeMillis) / 1000;
            log.info("Authenticated admin: {}, and generated access and refresh token", username);
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(expirationTimeSecs) // 7 days
                    .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());
            return new ResponseEntity<>(new ResponseTokenDTO(accessToken), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/new")
    public ResponseEntity<?> createUserOrAdmin(@RequestBody User user) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Creating user using admin: {}", authentication.getName());
            if (user.getRoles().contains(Role.ADMIN)) {
                return new ResponseEntity<>(userService.createAdmin(user), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
        } catch (Exception e)   {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
