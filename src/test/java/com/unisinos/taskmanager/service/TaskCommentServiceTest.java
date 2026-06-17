package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.TaskCommentCreateDTO;
import com.unisinos.taskmanager.exception.ForbiddenException;
import com.unisinos.taskmanager.model.*;
import com.unisinos.taskmanager.repository.BoardMemberRepository;
import com.unisinos.taskmanager.repository.TaskCommentRepository;
import com.unisinos.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @InjectMocks
    private TaskCommentService taskCommentService;

    // --- addComment ---

    @Test
    void addComment_whenMember_returnsCreatedComment() {
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();

        Board board = Board.builder().id(boardId).build();
        Task task = Task.builder().id(taskId).board(board).build();
        User requester = User.builder().id(requesterId).name("Author").email("a@t.com").build();
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskCommentCreateDTO dto = TaskCommentCreateDTO.builder().text("Nice work!").build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(taskCommentRepository.save(any(TaskComment.class))).thenAnswer(inv -> {
            TaskComment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        TaskComment result = taskCommentService.addComment(taskId, dto, requesterId, requester);

        assertThat(result.getContent()).isEqualTo("Nice work!");
        assertThat(result.getUser()).isEqualTo(requester);
        assertThat(result.getTask()).isEqualTo(task);
        verify(taskCommentRepository).save(any(TaskComment.class));
    }

    @Test
    void addComment_whenTaskNotFound_throwsResponseStatusException() {
        UUID taskId = UUID.randomUUID();
        TaskCommentCreateDTO dto = TaskCommentCreateDTO.builder().text("Comment").build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.addComment(taskId, dto, UUID.randomUUID(), null))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void addComment_whenNotMember_throwsForbiddenException() {
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();

        Board board = Board.builder().id(boardId).build();
        Task task = Task.builder().id(taskId).board(board).build();
        TaskCommentCreateDTO dto = TaskCommentCreateDTO.builder().text("Comment").build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.addComment(taskId, dto, requesterId, null))
                .isInstanceOf(ForbiddenException.class);
    }

    // --- deleteComment ---

    @Test
    void deleteComment_whenAuthor_deletesComment() {
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        User author = User.builder().id(authorId).build();
        TaskComment comment = TaskComment.builder().id(commentId).user(author).content("text").build();

        when(taskCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        taskCommentService.deleteComment(commentId, authorId);

        verify(taskCommentRepository).delete(comment);
    }

    @Test
    void deleteComment_whenNotAuthor_throwsForbiddenException() {
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User author = User.builder().id(authorId).build();
        TaskComment comment = TaskComment.builder().id(commentId).user(author).content("text").build();

        when(taskCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> taskCommentService.deleteComment(commentId, otherUserId))
                .isInstanceOf(ForbiddenException.class);

        verify(taskCommentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_whenNotFound_throwsResponseStatusException() {
        UUID commentId = UUID.randomUUID();
        when(taskCommentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.deleteComment(commentId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }
}
