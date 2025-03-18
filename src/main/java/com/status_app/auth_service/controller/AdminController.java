package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.CreateUserDTO;
import com.status_app.auth_service.dto.ResponseDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.entity.Role;
import com.status_app.auth_service.service.IAuthService;
import com.status_app.auth_service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private final IUserService userService;
    private final IAuthService authService;

    @Autowired
    public AdminController(IUserService userService, IAuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/new")
    public ResponseEntity<ResponseDTO<UserDTO>> createUserOrAdmin(@RequestBody CreateUserDTO user) {
        try {
            Authentication authentication = authService.getAuthContext();
            log.info("Creating user using admin: {}", authentication.getName());
            if (user.getRoles().contains(Role.ADMIN)) {
                ResponseDTO<UserDTO> resp = new ResponseDTO<>(userService.createAdmin(user), "Admin user created", "Success");
                return new ResponseEntity<>(resp, HttpStatus.CREATED);
            }
            ResponseDTO<UserDTO> userDTO = new ResponseDTO<>(userService.createUser(user), "User created", "Success");
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseDTO<UserDTO> resp = new ResponseDTO<>(null, "Could not create user.", "Failed");
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }
}
