package com.status_app.auth_service.service;

import com.status_app.auth_service.dto.CreateUserDTO;
import com.status_app.auth_service.dto.UserDTO;
import org.springframework.security.core.Authentication;

public interface IUserService {
    public UserDTO getAuthenticatedUser(Authentication authentication);
    public UserDTO createUser(CreateUserDTO userDTO);
    public UserDTO createAdmin(CreateUserDTO userDTO);
    public UserDTO getUser(String username);
    public UserDTO getUserByEmail(String email);
}
