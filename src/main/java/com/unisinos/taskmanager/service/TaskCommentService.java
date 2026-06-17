package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.TaskCommentCreateDTO;
import com.unisinos.taskmanager.dto.TaskCommentResponseDTO;
import com.unisinos.taskmanager.exception.ForbiddenException;
import com.unisinos.taskmanager.model.Task;
import com.unisinos.taskmanager.model.TaskComment;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.BoardMemberRepository;
import com.unisinos.taskmanager.repository.TaskCommentRepository;
import com.unisinos.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final BoardMemberRepository boardMemberRepository;

    public TaskComment addComment(UUID taskId, TaskCommentCreateDTO dto, UUID requesterId, User requester) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        UUID boardId = task.getBoard().getId();
        boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Only board members can comment on this task"));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(requester)
                .content(dto.getText())
                .build();

        return taskCommentRepository.save(comment);
    }

    public void deleteComment(UUID commentId, UUID requesterId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getId().equals(requesterId)) {
            log.warn("Forbidden: user {} tried to delete comment {} (author: {})", requesterId, commentId, comment.getUser().getId());
            throw new ForbiddenException("You can only delete your own comments");
        }

        taskCommentRepository.delete(comment);
        log.debug("Comment deleted: {}", commentId);
    }

    public List<TaskComment> findByTaskId(UUID taskId) {
        return taskCommentRepository.findByTask_IdOrderByCreatedAtAsc(taskId);
    }

    public TaskCommentResponseDTO toDto(TaskComment comment) {
        return TaskCommentResponseDTO.builder()
                .id(comment.getId())
                .text(comment.getContent())
                .authorId(comment.getUser() != null ? comment.getUser().getId() : null)
                .authorName(comment.getUser() != null ? comment.getUser().getName() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}


