package org.example.ash.controller;

import lombok.RequiredArgsConstructor;
import org.example.ash.dto.request.LoginRequest;
import org.example.ash.dto.request.RegisterRequest;
import org.example.ash.dto.response.BaseResponse;
import org.example.ash.entity.User;
import org.example.ash.service.AuthService;
import org.example.ash.service.UserDetailsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<?>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(BaseResponse.ok(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<?>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created(authService.register(request)));
    }

    @PostMapping("/user")
    public ResponseEntity<BaseResponse<?>> createUser(@RequestBody User user) {
        return ResponseEntity.ok(BaseResponse.ok(userDetailsService.createUser(user)));
    }
}
