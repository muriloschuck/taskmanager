package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.*;
import com.unisinos.taskmanager.model.Board;
import com.unisinos.taskmanager.model.BoardMember;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import com.unisinos.taskmanager.service.BoardService;
import com.unisinos.taskmanager.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import java.util.UUID;

@Slf4j
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
        log.info("Member {} added to board {}", addMemberDTO.getEmail(), boardId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping
    public ResponseEntity<BoardResponseDTO> createBoard(@Valid @RequestBody BoardCreateDTO createDTO) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        log.info("Creating board '{}' for user: {}", createDTO.getName(), requester.getId());
        Board created = boardService.createBoard(createDTO, requester.getId());
        BoardResponseDTO dto = BoardResponseDTO.builder()
                .id(created.getId())
                .name(created.getName())
                .description(created.getDescription())
                .ownerId(created.getOwner() != null ? created.getOwner().getId() : null)
                .createdAt(created.getCreatedAt())
                .updatedAt(created.getUpdatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<BoardResponseDTO>> listBoards() {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        List<Board> boards = boardService.getUserBoards(requester.getId());
        List<BoardResponseDTO> dtos = boards.stream().map(b -> BoardResponseDTO.builder()
                .id(b.getId())
                .name(b.getName())
                .description(b.getDescription())
                .ownerId(b.getOwner() != null ? b.getOwner().getId() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") UUID boardId) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        boardService.deleteBoard(boardId, requester.getId());
        log.info("Board deleted: {}", boardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<BoardMemberResponseDTO>> listMembers(@PathVariable("id") UUID boardId) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        // Verify requester is a member
        boardService.requireMember(boardId, requester.getId());
        List<BoardMember> members = boardService.listMembers(boardId);
        List<BoardMemberResponseDTO> dtos = members.stream().map(m -> BoardMemberResponseDTO.builder()
                .userId(m.getUser().getId())
                .userName(m.getUser().getName())
                .role(m.getRole())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable("id") UUID boardId, @PathVariable("userId") UUID userIdToRemove) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);

        boardService.removeMember(boardId, userIdToRemove, requester.getId());
        log.info("Member {} removed from board {}", userIdToRemove, boardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable("id") UUID boardId,
            @PathVariable("userId") UUID targetUserId,
            @Valid @RequestBody ChangeRoleDTO dto) {

        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);

        boardService.updateMemberRole(boardId, targetUserId, dto.getRole(), requester.getId());

        return ResponseEntity.noContent().build();
    }
}


