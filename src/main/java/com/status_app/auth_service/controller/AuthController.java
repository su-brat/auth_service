package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.LoginRequestDTO;
import com.status_app.auth_service.dto.AuthTokenDTO;
import com.status_app.auth_service.dto.ResponseDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.service.IAuthService;
import com.status_app.auth_service.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final IUserService userService;

    private final IAuthService authService;

    @Autowired
    public AuthController(IUserService userService, IAuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseDTO<UserDTO>> authenticate() {
        try {
            Authentication authentication = authService.getAuthContext();
            UserDTO user = userService.getAuthenticatedUser(authentication);
            ResponseDTO<UserDTO> resp = new ResponseDTO<>(user, "Verified user.", "Success");
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unable to authenticate: {}", e.getMessage(), e);
            ResponseDTO<UserDTO> resp = new ResponseDTO<>(null, "Could not verify user.", "Failed");
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<AuthTokenDTO>> refreshToken(@CookieValue(value = "refreshToken", required = true) String refreshToken) {
        ResponseDTO<AuthTokenDTO> resp;
        try {
            AuthTokenDTO authTokenDTO = authService.loginWithRefreshToken(refreshToken);
            resp = new ResponseDTO<>(authTokenDTO, "Token refreshed", "Success");
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resp = new ResponseDTO<>(null, "Could not generate token", "Failed");
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<AuthTokenDTO>> login(@RequestBody LoginRequestDTO requestBody, HttpServletResponse response) {
        try {
            String username = requestBody.getUsername();
            String password = requestBody.getPassword();
            if (username == null) {
                String email = requestBody.getEmail();
                if (email == null) {
                    throw new BadRequestException("Bad credentials");
                }
                AuthTokenDTO authTokenDTO = authService.loginWithEmail(email, password, response);
                ResponseDTO<AuthTokenDTO> resp = new ResponseDTO<>(authTokenDTO, "Logged in successfully", "Success");
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            AuthTokenDTO authTokenDTO = authService.loginWithUsername(username, password, response);
            ResponseDTO<AuthTokenDTO> resp = new ResponseDTO<>(authTokenDTO, "Logged in successfully", "Success");
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseDTO<AuthTokenDTO> resp = new ResponseDTO<>(null, "Log in failed", "Failed");
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }
    }

}