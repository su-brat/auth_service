package com.status_app.auth_service.service;

import com.status_app.auth_service.entity.Role;
import com.status_app.auth_service.entity.User;
import com.status_app.auth_service.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    @Autowired
    IUserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User createUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Set.of(Role.USER));
            log.info("Creating user: {}", user);
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public User createAdmin(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Set.of(Role.ADMIN));
            log.info("Creating admin: {}", user);
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("Error creating admin: {}", e.getMessage(), e);
            throw e;
        }
    }

    public User getUser(String username) {
        try {
            log.info("Logging in user: {}", username);
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("Error logging in user: {}", e.getMessage(), e);
            throw e;
        }
    }

}
