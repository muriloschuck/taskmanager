package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.UserRegisterDTO;
import com.unisinos.taskmanager.dto.UserUpdateDTO;
import com.unisinos.taskmanager.exception.DuplicateEmailException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserRegisterDTO registerDTO) {
      if (userRepository.findByEmailAndDeletedFalse(registerDTO.getEmail()).isPresent()) {
            log.warn("Registration rejected: email already exists: {}", registerDTO.getEmail());
            throw new DuplicateEmailException("Email already exists");
        }

        User user = User.builder()
                .name(registerDTO.getName())
                .email(registerDTO.getEmail())
                .passwordHash(passwordEncoder.encode(registerDTO.getPassword()))
                .deleted(false)
                .build();

        User saved = userRepository.save(user);
        log.info("User created: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User updateUser(UUID id, UserUpdateDTO updateDTO) {
        User user = getUserById(id);

        if (updateDTO.getName() != null && !updateDTO.getName().isBlank()) {
            user.setName(updateDTO.getName());
        }

        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        User user = getUserById(id);
        user.setDeleted(true);
        userRepository.save(user);
        log.info("User soft-deleted: id={}, email={}", id, user.getEmail());
    }
}