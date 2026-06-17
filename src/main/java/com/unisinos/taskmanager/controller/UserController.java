package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.UserRegisterDTO;
import com.unisinos.taskmanager.dto.UserResponseDTO;
import com.unisinos.taskmanager.dto.UserUpdateDTO;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("Registering new user with email: {}", registerDTO.getEmail());
        User user = userService.createUser(registerDTO);
        log.info("User registered successfully: id={}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(user));
    }

    private void verifyOwnership(UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String currentPrincipalName = authentication.getName();
        User targetUser = userService.getUserById(id);
        if (!targetUser.getEmail().equals(currentPrincipalName)) {
            log.warn("Ownership denied: user {} tried to access user {}", currentPrincipalName, id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Cannot manipulate other users' data.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        verifyOwnership(id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateDTO updateDTO) {
        verifyOwnership(id);
        User updatedUser = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        verifyOwnership(id);
        userService.deleteUser(id);
        log.info("User soft-deleted: {}", id);
        return ResponseEntity.noContent().build();
    }
}