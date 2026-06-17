package com.unisinos.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskCommentResponseDTO {
    private UUID id;
    private String text;
    private UUID authorId;
    private String authorName;
    private LocalDateTime createdAt;
}

