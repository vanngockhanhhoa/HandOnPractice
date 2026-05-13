package org.example.ash.dto.request;

import lombok.Data;
import org.example.ash.entity.Role;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Role role = Role.ROLE_USER;
}