package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.UserRegisterDTO;
import com.unisinos.taskmanager.dto.UserUpdateDTO;
import com.unisinos.taskmanager.entity.User;
import com.unisinos.taskmanager.exception.DuplicateEmailException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserRegisterDTO registerDTO) {
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = getUserById(id);

        if (updateDTO.getName() != null && !updateDTO.getName().isBlank()) {
            user.setName(updateDTO.getName());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }
}