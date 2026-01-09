package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.dto.UserLoginDTO;
import com.itt.newsaggregator.dto.UserLoginResponseDTO;
import com.itt.newsaggregator.dto.UserRegisterDTO;
import com.itt.newsaggregator.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserLoginResponseDTO> registerUser(@Valid @RequestBody UserRegisterDTO dto) {
        UserLoginResponseDTO response = authService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            UserLoginResponseDTO response = authService.loginUser(loginDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
