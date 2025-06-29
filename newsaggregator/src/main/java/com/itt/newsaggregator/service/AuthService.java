package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.UserLoginDTO;
import com.itt.newsaggregator.dto.UserLoginResponseDTO;
import com.itt.newsaggregator.dto.UserRegisterDTO;
import com.itt.newsaggregator.entities.Role;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.RoleRepository;
import com.itt.newsaggregator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UserLoginResponseDTO registerUser(UserRegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        return new UserLoginResponseDTO(savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole().getName());
    }

    public UserLoginResponseDTO loginUser(UserLoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return new UserLoginResponseDTO(user.getUsername(), user.getEmail(), user.getRole().getName());
    }



}
