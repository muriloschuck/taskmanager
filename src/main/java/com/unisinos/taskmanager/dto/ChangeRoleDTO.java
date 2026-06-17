package com.unisinos.taskmanager.dto;

import com.unisinos.taskmanager.model.enums.BoardRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleDTO {
    @NotNull(message = "Role is required")
    private BoardRole role;
}