package org.example.ash.service;

import lombok.RequiredArgsConstructor;
import org.example.ash.dto.request.LoginRequest;
import org.example.ash.dto.request.RegisterRequest;
import org.example.ash.dto.response.LoginResponse;
import org.example.ash.entity.User;
import org.example.ash.exception.AppException;
import org.example.ash.repository.IUserRepo;
import org.example.ash.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserRepo             userRepo;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authManager;

    public LoginResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("User not found").status(HttpStatus.NOT_FOUND.value()));

        String token = jwtTokenProvider.generate(user);
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new AppException("Username already taken").status(HttpStatus.CONFLICT.value());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepo.save(user);

        String token = jwtTokenProvider.generate(user);
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
