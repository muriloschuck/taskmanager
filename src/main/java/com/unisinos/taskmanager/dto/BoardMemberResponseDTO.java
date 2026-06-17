package com.unisinos.taskmanager.dto;

import com.unisinos.taskmanager.model.enums.BoardRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardMemberResponseDTO {
    private UUID userId;
    private String userName;
    private BoardRole role;
}

