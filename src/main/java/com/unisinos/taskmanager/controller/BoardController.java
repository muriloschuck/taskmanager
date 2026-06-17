package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.AddMemberDTO;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import com.unisinos.taskmanager.service.BoardService;
import com.unisinos.taskmanager.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable("id") UUID boardId, @Valid @RequestBody AddMemberDTO addMemberDTO) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);

        boardService.addMember(boardId, addMemberDTO.getEmail(), requester.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable("id") UUID boardId, @PathVariable("userId") UUID userIdToRemove) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);

        boardService.removeMember(boardId, userIdToRemove, requester.getId());
        return ResponseEntity.noContent().build();
    }
}


