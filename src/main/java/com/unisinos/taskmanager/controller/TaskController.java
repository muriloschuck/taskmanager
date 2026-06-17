package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.TaskCreateDTO;
import com.unisinos.taskmanager.dto.TaskResponseDTO;
import com.unisinos.taskmanager.dto.TaskUpdateDTO;
import com.unisinos.taskmanager.model.Task;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.model.enums.TaskStatus;
import com.unisinos.taskmanager.repository.UserRepository;
import com.unisinos.taskmanager.service.TaskService;
import com.unisinos.taskmanager.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreateDTO createDTO) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        Task created = taskService.createTask(createDTO, requester.getId());
        log.info("Task created: '{}' in board {}", created.getTitle(), createDTO.getBoardId());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.toDto(created));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam(value = "boardId", required = false) UUID boardId,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "assignedUserId", required = false) UUID assignedUserId,
            @RequestParam(value = "search", required = false) String search
    ) {
        // convert status string to enum if present
        TaskStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = TaskStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid task status filter: '{}'", statusStr);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
            }
        }

        List<Task> tasks = taskService.getFilteredTasks(boardId, status, assignedUserId, search);
        List<TaskResponseDTO> dtos = tasks.stream().map(taskService::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTaskPartial(@PathVariable UUID id, @RequestBody TaskUpdateDTO updateDTO) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        Task updated = taskService.updateTaskPartial(id, updateDTO, requester.getId());
        log.info("Task {} updated by user {}", id, requester.getId());
        return ResponseEntity.ok(taskService.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        User requester = SecurityUtils.getAuthenticatedRequester(userRepository);
        taskService.deleteTask(id, requester.getId());
        log.info("Task {} deleted by user {}", id, requester.getId());
        return ResponseEntity.noContent().build();
    }
}


