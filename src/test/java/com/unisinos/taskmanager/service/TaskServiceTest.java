package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.TaskCreateDTO;
import com.unisinos.taskmanager.dto.TaskUpdateDTO;
import com.unisinos.taskmanager.exception.ForbiddenException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.Board;
import com.unisinos.taskmanager.model.BoardMember;
import com.unisinos.taskmanager.model.Task;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.model.enums.TaskPriority;
import com.unisinos.taskmanager.model.enums.TaskStatus;
import com.unisinos.taskmanager.repository.BoardMemberRepository;
import com.unisinos.taskmanager.repository.BoardRepository;
import com.unisinos.taskmanager.repository.TaskRepository;
import com.unisinos.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User buildUser(UUID id) {
        return User.builder().id(id).email("user@test.com").name("Test").deleted(false).build();
    }

    private Board buildBoard(UUID id) {
        return Board.builder().id(id).name("Board").build();
    }

    private Task buildTask(UUID id, Board board) {
        return Task.builder().id(id).title("Task").board(board).status(TaskStatus.PENDING).priority(TaskPriority.MEDIUM).build();
    }

    // --- createTask ---

    @Test
    void createTask_whenMember_returnsCreatedTask() {
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskCreateDTO dto = TaskCreateDTO.builder()
                .title("New Task")
                .description("Description")
                .boardId(boardId)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.PENDING)
                .build();

        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Task result = taskService.createTask(dto, requesterId);

        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getBoard()).isEqualTo(board);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_whenNotMember_throwsForbiddenException() {
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();

        TaskCreateDTO dto = TaskCreateDTO.builder().title("Task").boardId(boardId).build();

        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(dto, requesterId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createTask_whenBoardNotFound_throwsResponseStatusException() {
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskCreateDTO dto = TaskCreateDTO.builder().title("Task").boardId(boardId).build();

        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(dto, requesterId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createTask_whenAssignedUserNotFound_throwsUserNotFoundException() {
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        UUID assignedId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskCreateDTO dto = TaskCreateDTO.builder()
                .title("Task")
                .boardId(boardId)
                .assignedUserId(assignedId)
                .build();

        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(userRepository.findById(assignedId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(dto, requesterId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createTask_withoutAssignedUser_createsTaskWithNullAssignee() {
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskCreateDTO dto = TaskCreateDTO.builder()
                .title("Task")
                .boardId(boardId)
                .assignedUserId(null)
                .build();

        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(dto, requesterId);

        assertThat(result.getAssignedUser()).isNull();
    }

    @Test
    void createTask_withoutStatusAndPriority_usesDefaults() {
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskCreateDTO dto = TaskCreateDTO.builder()
                .title("Task")
                .boardId(boardId)
                .status(null)
                .priority(null)
                .build();

        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(dto, requesterId);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    // --- deleteTask ---

    @Test
    void deleteTask_whenMember_deletesTask() {
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        Task task = buildTask(taskId, board);
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));

        taskService.deleteTask(taskId, requesterId);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_whenTaskNotFound_throwsResponseStatusException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(taskId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteTask_whenNotMember_throwsForbiddenException() {
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        Task task = buildTask(taskId, board);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(taskId, requesterId))
                .isInstanceOf(ForbiddenException.class);
    }

    // --- updateTaskPartial ---

    @Test
    void updateTaskPartial_updatesOnlyProvidedFields() {
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        Task task = Task.builder().id(taskId).title("Old").description("Old desc").board(board)
                .status(TaskStatus.PENDING).priority(TaskPriority.LOW).build();
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskUpdateDTO dto = TaskUpdateDTO.builder().title("New Title").status(TaskStatus.IN_PROGRESS).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTaskPartial(taskId, dto, requesterId);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getDescription()).isEqualTo("Old desc"); // unchanged
        assertThat(result.getPriority()).isEqualTo(TaskPriority.LOW); // unchanged
    }

    @Test
    void updateTaskPartial_updatesAllFields() {
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        UUID assignedId = UUID.randomUUID();
        Board board = buildBoard(boardId);
        User assigned = buildUser(assignedId);
        Task task = Task.builder().id(taskId).title("Old").board(board)
                .status(TaskStatus.PENDING).priority(TaskPriority.LOW).build();
        BoardMember member = BoardMember.builder().id(UUID.randomUUID()).build();

        TaskUpdateDTO dto = TaskUpdateDTO.builder()
                .title("New")
                .description("New desc")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.of(2026, 12, 31))
                .assignedUserId(assignedId)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)).thenReturn(Optional.of(member));
        when(userRepository.findById(assignedId)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTaskPartial(taskId, dto, requesterId);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.getAssignedUser()).isEqualTo(assigned);
    }
}
