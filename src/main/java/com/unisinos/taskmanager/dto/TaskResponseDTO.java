package com.unisinos.taskmanager.dto;

import com.unisinos.taskmanager.model.enums.TaskPriority;
import com.unisinos.taskmanager.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private UUID boardId;
    private UUID assignedUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

