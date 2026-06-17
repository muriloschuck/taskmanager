package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.AddMemberDTO;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import com.unisinos.taskmanager.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable("id") UUID boardId, @Valid @RequestBody AddMemberDTO addMemberDTO) {
        User requester = getAuthenticatedRequester();

        boardService.addMember(boardId, addMemberDTO.getEmail(), requester.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable("id") UUID boardId, @PathVariable("userId") UUID userIdToRemove) {
        User requester = getAuthenticatedRequester();

        boardService.removeMember(boardId, userIdToRemove, requester.getId());
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedRequester() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String principalEmail = authentication.getName();
        return userRepository.findByEmailAndDeletedFalse(principalEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}


