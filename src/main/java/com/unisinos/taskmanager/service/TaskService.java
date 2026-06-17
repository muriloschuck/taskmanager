package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.TaskCreateDTO;
import com.unisinos.taskmanager.dto.TaskResponseDTO;
import com.unisinos.taskmanager.dto.TaskUpdateDTO;
import com.unisinos.taskmanager.exception.ForbiddenException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.Board;
import com.unisinos.taskmanager.model.Task;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.model.enums.TaskPriority;
import com.unisinos.taskmanager.model.enums.TaskStatus;
import com.unisinos.taskmanager.repository.BoardMemberRepository;
import com.unisinos.taskmanager.repository.BoardRepository;
import com.unisinos.taskmanager.repository.TaskRepository;
import com.unisinos.taskmanager.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public List<Task> getFilteredTasks(UUID boardId, TaskStatus status, UUID assignedUserId, String search) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (boardId != null) {
                predicates.add(cb.equal(root.get("board").get("id"), boardId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (assignedUserId != null) {
                predicates.add(cb.equal(root.get("assignedUser").get("id"), assignedUserId));
            }

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec);
    }

    public Task createTask(TaskCreateDTO dto, UUID requesterId) {
        // Verify requester is member of the board
        UUID boardId = dto.getBoardId();
        boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)
                .orElseThrow(() -> {
                    log.warn("Forbidden: user {} is not a member of board {}", requesterId, boardId);
                    return new ForbiddenException("Only board members can create tasks");
                });

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        User assigned = null;
        if (dto.getAssignedUserId() != null) {
            assigned = userRepository.findById(dto.getAssignedUserId())
                    .filter(u -> !u.isDeleted())
                    .orElseThrow(() -> new UserNotFoundException("Assigned user not found"));
        }

        TaskStatus status = dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING;
        TaskPriority priority = dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM;

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(status)
                .priority(priority)
                .dueDate(dto.getDueDate())
                .board(board)
                .assignedUser(assigned)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created: id={}, board={}, title='{}'", saved.getId(), boardId, saved.getTitle());
        return saved;
    }

    public void deleteTask(UUID taskId, UUID requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        UUID boardId = task.getBoard().getId();
        boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Only board members can delete tasks"));

        taskRepository.delete(task);
        log.info("Task deleted: id={}", taskId);
    }

    public Task updateTaskPartial(UUID taskId, TaskUpdateDTO dto, UUID requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        UUID boardId = task.getBoard().getId();
        boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Only board members can update tasks"));

        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
        if (dto.getAssignedUserId() != null) {
            User assigned = userRepository.findById(dto.getAssignedUserId())
                    .filter(u -> !u.isDeleted())
                    .orElseThrow(() -> new UserNotFoundException("Assigned user not found"));
            task.setAssignedUser(assigned);
        }

        return taskRepository.save(task);
    }

    public TaskResponseDTO toDto(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .boardId(task.getBoard() != null ? task.getBoard().getId() : null)
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}


