package com.status_app.auth_service.dto;

import com.status_app.auth_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    @Id
    private ObjectId id;

    @NonNull
    private String username;

    private String name;

    @NonNull
    private String email;

    @NonNull
    private String password;

    private Set<Role> roles;
}
