package org.example.ash.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ash.entity.Role;

@Getter
@Builder
public class LoginResponse {
    private final String token;
    private final String tokenType = "Bearer";
    private final String username;
    private final Role role;
}