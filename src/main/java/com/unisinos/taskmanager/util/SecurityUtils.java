package com.unisinos.taskmanager.util;

import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class SecurityUtils {

    private SecurityUtils() {
        // Utility class - no instantiation
    }

    public static User getAuthenticatedRequester(UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String principalEmail = authentication.getName();
        return userRepository.findByEmailAndDeletedFalse(principalEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

