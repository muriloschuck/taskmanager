package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.TaskCommentCreateDTO;
import com.unisinos.taskmanager.dto.TaskCommentResponseDTO;
import com.unisinos.taskmanager.model.TaskComment;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import com.unisinos.taskmanager.service.TaskCommentService;
import com.unisinos.taskmanager.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;
    private final UserRepository userRepository;

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<TaskCommentResponseDTO> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskCommentCreateDTO createDTO
    ) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        TaskComment created = taskCommentService.addComment(taskId, createDTO, requester.getId(), requester);
        log.debug("Comment added to task {} by user {}", taskId, requester.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCommentService.toDto(created));
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<List<TaskCommentResponseDTO>> getComments(@PathVariable UUID taskId) {
        List<TaskComment> comments = taskCommentService.findByTaskId(taskId);
        List<TaskCommentResponseDTO> dtos = comments.stream()
                .map(taskCommentService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        taskCommentService.deleteComment(commentId, requester.getId());
        log.debug("Comment {} deleted by user {}", commentId, requester.getId());
        return ResponseEntity.noContent().build();
    }
}

