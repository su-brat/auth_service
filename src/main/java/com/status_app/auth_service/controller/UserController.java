package com.status_app.auth_service.controller;

import com.status_app.auth_service.dto.CreateUserDTO;
import com.status_app.auth_service.dto.ResponseDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/new")
    public ResponseEntity<ResponseDTO<UserDTO>> signup(@RequestBody CreateUserDTO user) {
        try {
            ResponseDTO<UserDTO> responseDTO = new ResponseDTO<>(userService.createUser(user), "User created", "Success");
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseDTO<UserDTO> responseDTO = new ResponseDTO<>(null, "User could not be created", "Failed");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
    }
}
