package com.status_app.auth_service.service;

import com.status_app.auth_service.dto.CreateUserDTO;
import com.status_app.auth_service.dto.UserDTO;
import com.status_app.auth_service.entity.Role;
import com.status_app.auth_service.entity.User;
import com.status_app.auth_service.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    ModelMapper mapper;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDTO createUser(CreateUserDTO userDTO) {
        try {
            User user = mapper.map(userDTO, User.class);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Set.of(Role.USER));
            log.info("Creating user: {}", user);
            userRepository.save(user);
            return mapper.map(user, UserDTO.class);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserDTO createAdmin(CreateUserDTO userDTO) {
        try {
            User user = mapper.map(userDTO, User.class);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Set.of(Role.USER, Role.ADMIN));
            log.info("Creating admin: {}", user);
            userRepository.save(user);
            return mapper.map(user, UserDTO.class);
        } catch (Exception e) {
            log.error("Error creating admin: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserDTO getUser(String username) {
        try {
            log.info("Logging in user: {}", username);
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }
            return mapper.map(user, UserDTO.class);
        } catch (Exception e) {
            log.error("Error logging in user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserDTO getUserByEmail(String email) {
        try {
            log.info("Logging in user by email: {}", email);
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            return mapper.map(user, UserDTO.class);
        } catch (Exception e) {
            log.error("Error logging in user by email: {}", e.getMessage(), e);
            throw e;
        }
    }

}
