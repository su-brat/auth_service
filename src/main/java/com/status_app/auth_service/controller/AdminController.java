package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.CreateUserDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.entity.Role;
import com.status_app.auth_service.service.UserService;
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

    @Autowired
    private UserService userService;

    @PostMapping("/new")
    public ResponseEntity<UserDTO> createUserOrAdmin(@RequestBody CreateUserDTO user) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Creating user using admin: {}", authentication.getName());
            if (user.getRoles().contains(Role.ADMIN)) {
                return new ResponseEntity<>(userService.createAdmin(user), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
