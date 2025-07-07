package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.UserLoginDTO;
import com.itt.newsaggregator.dto.UserLoginResponseDTO;
import com.itt.newsaggregator.dto.UserRegisterDTO;
import com.itt.newsaggregator.entities.Role;
import com.itt.newsaggregator.entities.User;
import com.itt.newsaggregator.repository.RoleRepository;
import com.itt.newsaggregator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("registerUser succeeds with valid data")
    void registerUser_Success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("user");
        dto.setEmail("user@email.com");
        dto.setPassword("pass");
        Mockito.when(userRepository.existsByUsername("user")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("pass")).thenReturn("hashed");
        Role role = new Role(); role.setName("USER");
        Mockito.when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        User user = new User(); user.setId(1L); user.setUsername("user"); user.setEmail("user@email.com"); user.setPassword("hashed"); user.setRole(role);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        UserLoginResponseDTO response = authService.registerUser(dto);
        assertEquals("user", response.getUsername());
        assertEquals("user@email.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }

    @Test
    @DisplayName("registerUser throws if username exists")
    void registerUser_ThrowsIfUsernameExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("user");
        Mockito.when(userRepository.existsByUsername("user")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.registerUser(dto));
    }

    @Test
    @DisplayName("registerUser throws if email exists")
    void registerUser_ThrowsIfEmailExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("user");
        dto.setEmail("user@email.com");
        Mockito.when(userRepository.existsByUsername("user")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("user@email.com")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.registerUser(dto));
    }

    @Test
    @DisplayName("registerUser throws if role not found")
    void registerUser_ThrowsIfRoleNotFound() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("user");
        dto.setEmail("user@email.com");
        Mockito.when(userRepository.existsByUsername("user")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
        Mockito.when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.registerUser(dto));
    }

    @Test
    @DisplayName("loginUser succeeds with correct credentials")
    void loginUser_Success() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("user");
        dto.setPassword("pass");
        User user = new User(); user.setId(1L); user.setUsername("user"); user.setEmail("user@email.com"); user.setPassword("hashed");
        Role role = new Role(); role.setName("USER"); user.setRole(role);
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        UserLoginResponseDTO response = authService.loginUser(dto);
        assertEquals("user", response.getUsername());
        assertEquals("user@email.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }

    @Test
    @DisplayName("loginUser throws if user not found")
    void loginUser_ThrowsIfUserNotFound() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("user");
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.loginUser(dto));
    }

    @Test
    @DisplayName("loginUser throws if password does not match")
    void loginUser_ThrowsIfPasswordMismatch() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("user");
        dto.setPassword("wrong");
        User user = new User(); user.setId(1L); user.setUsername("user"); user.setPassword("hashed");
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.loginUser(dto));
    }
}
