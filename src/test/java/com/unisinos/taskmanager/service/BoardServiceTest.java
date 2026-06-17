package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.BoardCreateDTO;
import com.unisinos.taskmanager.exception.ForbiddenException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.Board;
import com.unisinos.taskmanager.model.BoardMember;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.model.enums.BoardRole;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private BoardService boardService;

    private User buildUser(UUID id) {
        return User.builder().id(id).email("user@test.com").name("Test").passwordHash("hash").deleted(false).build();
    }

    private Board buildBoard(UUID id, User owner) {
        return Board.builder().id(id).name("Board").description("Desc").owner(owner).build();
    }

    private BoardMember buildMember(UUID boardId, UUID userId, BoardRole role) {
        Board board = Board.builder().id(boardId).name("Board").build();
        User user = buildUser(userId);
        return BoardMember.builder().id(UUID.randomUUID()).board(board).user(user).role(role).build();
    }

    // --- createBoard ---

    @Test
    void createBoard_withValidData_returnsBoardAndCreatesOwnerMembership() {
        UUID requesterId = UUID.randomUUID();
        User owner = buildUser(requesterId);
        BoardCreateDTO dto = BoardCreateDTO.builder().name("My Board").description("Description").build();

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(owner));
        when(boardRepository.save(any(Board.class))).thenAnswer(inv -> {
            Board b = inv.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });
        when(boardMemberRepository.save(any(BoardMember.class))).thenAnswer(inv -> inv.getArgument(0));

        Board result = boardService.createBoard(dto, requesterId);

        assertThat(result.getName()).isEqualTo("My Board");
        assertThat(result.getOwner()).isEqualTo(owner);
        verify(boardMemberRepository).save(argThat(member -> member.getRole() == BoardRole.OWNER));
    }

    @Test
    void createBoard_whenRequesterNotFound_throwsUserNotFoundException() {
        UUID requesterId = UUID.randomUUID();
        BoardCreateDTO dto = BoardCreateDTO.builder().name("Board").build();

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.createBoard(dto, requesterId))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- getUserBoards ---

    @Test
    void getUserBoards_whenUserHasBoards_returnsList() {
        UUID userId = UUID.randomUUID();
        Board board1 = Board.builder().id(UUID.randomUUID()).name("Board 1").build();
        Board board2 = Board.builder().id(UUID.randomUUID()).name("Board 2").build();
        BoardMember m1 = BoardMember.builder().board(board1).build();
        BoardMember m2 = BoardMember.builder().board(board2).build();

        when(boardMemberRepository.findByUser_Id(userId)).thenReturn(List.of(m1, m2));

        List<Board> result = boardService.getUserBoards(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getUserBoards_whenNoMemberships_returnsEmptyList() {
        UUID userId = UUID.randomUUID();
        when(boardMemberRepository.findByUser_Id(userId)).thenReturn(Collections.emptyList());

        List<Board> result = boardService.getUserBoards(userId);

        assertThat(result).isEmpty();
    }

    // --- deleteBoard ---

    @Test
    void deleteBoard_whenRequesterIsOwner_deletesEverything() {
        UUID boardId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Board board = buildBoard(boardId, owner);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(taskRepository.findByBoard_Id(boardId)).thenReturn(Collections.emptyList());
        when(boardMemberRepository.findByBoard_Id(boardId)).thenReturn(Collections.emptyList());

        boardService.deleteBoard(boardId, ownerId);

        verify(boardRepository).delete(board);
    }

    @Test
    void deleteBoard_whenRequesterIsNotOwner_throwsForbiddenException() {
        UUID boardId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Board board = buildBoard(boardId, owner);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        assertThatThrownBy(() -> boardService.deleteBoard(boardId, otherUserId))
                .isInstanceOf(ForbiddenException.class);

        verify(boardRepository, never()).delete(any());
    }

    @Test
    void deleteBoard_whenBoardNotFound_throwsResponseStatusException() {
        UUID boardId = UUID.randomUUID();
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.deleteBoard(boardId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }

    // --- addMember ---

    @Test
    void addMember_whenRequesterIsOwner_addsAsMember() {
        UUID boardId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        User newUser = User.builder().id(newUserId).email("new@test.com").name("New").deleted(false).build();
        Board board = Board.builder().id(boardId).name("Board").build();

        BoardMember requesterMember = buildMember(boardId, requesterId, BoardRole.OWNER);
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)).thenReturn(Optional.of(requesterMember));
        when(userRepository.findByEmailAndDeletedFalse("new@test.com")).thenReturn(Optional.of(newUser));
        when(boardMemberRepository.existsByBoard_IdAndUser_Id(boardId, newUserId)).thenReturn(false);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardMemberRepository.save(any(BoardMember.class))).thenAnswer(inv -> inv.getArgument(0));

        boardService.addMember(boardId, "new@test.com", requesterId);

        verify(boardMemberRepository).save(argThat(m -> m.getRole() == BoardRole.MEMBER));
    }

    @Test
    void addMember_whenRequesterIsMember_throwsForbiddenException() {
        UUID boardId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        BoardMember requesterMember = buildMember(boardId, requesterId, BoardRole.MEMBER);
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)).thenReturn(Optional.of(requesterMember));

        assertThatThrownBy(() -> boardService.addMember(boardId, "new@test.com", requesterId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void addMember_whenEmailNotFound_throwsUserNotFoundException() {
        UUID boardId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        BoardMember requesterMember = buildMember(boardId, requesterId, BoardRole.OWNER);
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)).thenReturn(Optional.of(requesterMember));
        when(userRepository.findByEmailAndDeletedFalse("nonexist@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.addMember(boardId, "nonexist@test.com", requesterId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addMember_whenAlreadyMember_throwsConflict() {
        UUID boardId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID existingUserId = UUID.randomUUID();
        User existingUser = User.builder().id(existingUserId).email("exists@test.com").deleted(false).build();

        BoardMember requesterMember = buildMember(boardId, requesterId, BoardRole.OWNER);
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)).thenReturn(Optional.of(requesterMember));
        when(userRepository.findByEmailAndDeletedFalse("exists@test.com")).thenReturn(Optional.of(existingUser));
        when(boardMemberRepository.existsByBoard_IdAndUser_Id(boardId, existingUserId)).thenReturn(true);

        assertThatThrownBy(() -> boardService.addMember(boardId, "exists@test.com", requesterId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already a member");
    }

    // --- removeMember ---

    @Test
    void removeMember_whenRequesterIsOwner_removesTarget() {
        UUID boardId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        BoardMember requesterMember = buildMember(boardId, requesterId, BoardRole.OWNER);
        BoardMember targetMember = buildMember(boardId, targetId, BoardRole.MEMBER);

        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)).thenReturn(Optional.of(requesterMember));
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, targetId)).thenReturn(Optional.of(targetMember));

        boardService.removeMember(boardId, targetId, requesterId);

        verify(boardMemberRepository).delete(targetMember);
    }

    @Test
    void removeMember_whenTargetNotFound_throwsResponseStatusException() {
        UUID boardId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        BoardMember requesterMember = buildMember(boardId, requesterId, BoardRole.OWNER);
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)).thenReturn(Optional.of(requesterMember));
        when(boardMemberRepository.findByBoard_IdAndUser_Id(boardId, targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.removeMember(boardId, targetId, requesterId))
                .isInstanceOf(ResponseStatusException.class);
    }
}
