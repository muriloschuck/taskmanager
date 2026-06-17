package com.unisinos.taskmanager.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;


    public Board createBoard(com.unisinos.taskmanager.dto.BoardCreateDTO dto, UUID requesterId) {
        User owner = userRepository.findById(requesterId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Board board = Board.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(owner)
                .build();

        Board saved = boardRepository.save(board);

        BoardMember ownerMember = BoardMember.builder()
                .board(saved)
                .user(owner)
                .role(BoardRole.OWNER)
                .build();

        boardMemberRepository.save(ownerMember);
        return saved;
    }

    public List<Board> getUserBoards(UUID userId) {
        return boardMemberRepository.findByUserId(userId).stream()
                .map(BoardMember::getBoard)
                .collect(Collectors.toList());
    }

    public void deleteBoard(UUID boardId, UUID requesterId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getOwner() == null || !board.getOwner().getId().equals(requesterId)) {
            throw new ForbiddenException("Only the owner can delete the board");
        }

        // delete tasks
        taskRepository.deleteAll(taskRepository.findByBoardId(boardId));

        // delete members
        boardMemberRepository.deleteAll(boardMemberRepository.findByBoardId(boardId));

        boardRepository.delete(board);
    }

    public void requireMember(UUID boardId, UUID requesterId) {
        boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));
    }

    public List<BoardMember> listMembers(UUID boardId) {
        return boardMemberRepository.findByBoardId(boardId);
    }

    /**
     * Adds a user (found by email) to the board as a MEMBER.
     * Enforces RBAC: requester must be OWNER or ADMIN of the board.
     */
    public void addMember(UUID boardId, String emailToAdd, UUID requesterId) {
        requireOwnerOrAdmin(boardId, requesterId);

        User userToAdd = userRepository.findByEmailAndDeletedFalse(emailToAdd)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, userToAdd.getId())) {
            log.warn("Conflict: user {} is already a member of board {}", emailToAdd, boardId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of the board");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        BoardMember newMember = BoardMember.builder()
                .board(board)
                .user(userToAdd)
                .role(BoardRole.MEMBER)
                .build();

        boardMemberRepository.save(newMember);
        log.info("Member added: user {} to board {} as MEMBER", userToAdd.getId(), boardId);
    }

    /**
     * Removes a member from a board. Enforces RBAC: requester must be OWNER or ADMIN.
     */
    public void removeMember(UUID boardId, UUID userIdToRemove, UUID requesterId) {
        requireOwnerOrAdmin(boardId, requesterId);

        BoardMember target = boardMemberRepository.findByBoardIdAndUserId(boardId, userIdToRemove)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        boardMemberRepository.delete(target);
        log.info("Member removed: user {} from board {}", userIdToRemove, boardId);
    }

    private void requireOwnerOrAdmin(UUID boardId, UUID requesterId) {
        BoardMember requesterMember = boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)
                .orElseThrow(() -> {
                    log.warn("Access denied: user {} is not a member of board {}", requesterId, boardId);
                    return new ForbiddenException("Access denied");
                });

        BoardRole role = requesterMember.getRole();
        if (role != BoardRole.OWNER && role != BoardRole.ADMIN) {
            log.warn("Access denied: user {} is not OWNER/ADMIN of board {} (role: {})", requesterId, boardId, role);
            throw new ForbiddenException("Access denied");
        }
    }
}




