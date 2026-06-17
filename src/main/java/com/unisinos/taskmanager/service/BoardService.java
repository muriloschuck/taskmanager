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
        return boardRepository.findBoardsByMemberId(userId);
    }

    public void deleteBoard(UUID boardId, UUID requesterId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getOwner() == null || !board.getOwner().getId().equals(requesterId)) {
            throw new ForbiddenException("Only the owner can delete the board");
        }

        // delete tasks
        taskRepository.deleteAll(taskRepository.findByBoard_Id(boardId));

        // delete members
        boardMemberRepository.deleteAll(boardMemberRepository.findByBoard_Id(boardId));

        boardRepository.delete(board);
    }

    public void requireMember(UUID boardId, UUID requesterId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));
        boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));
    }

    public List<BoardMember> listMembers(UUID boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));
        return boardMemberRepository.findByBoard_Id(boardId);
    }

    /**
     * Adds a user (found by email) to the board as a MEMBER.
     * Enforces RBAC: requester must be OWNER or ADMIN of the board.
     */
    public void addMember(UUID boardId, String emailToAdd, UUID requesterId) {
        requireOwnerOrAdmin(boardId, requesterId);

        User userToAdd = userRepository.findByEmailAndDeletedFalse(emailToAdd)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (boardMemberRepository.existsByBoard_IdAndUser_Id(boardId, userToAdd.getId())) {
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

        BoardMember target = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, userIdToRemove)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        boardMemberRepository.delete(target);
        log.info("Member removed: user {} from board {}", userIdToRemove, boardId);
    }

    private void requireOwnerOrAdmin(UUID boardId, UUID requesterId) {
        BoardMember requesterMember = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)
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

    /**
     * Updates the role of an existing member within a board.
     * Enforces strict RBAC (Role-Based Access Control) to prevent unauthorized privilege escalation.
     */
    public void updateMemberRole(UUID boardId, UUID targetUserId, BoardRole newRole, UUID requesterId) {

        BoardMember requesterMember = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Access denied: You are not a member of this board."));

        if (requesterMember.getRole() == BoardRole.MEMBER) {
            log.warn("Access denied: User {} tried to change roles in board {} without ADMIN/OWNER privileges.", requesterId, boardId);
            throw new ForbiddenException("Only ADMINs or OWNERs can change member roles.");
        }

        BoardMember targetMember = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target member not found in this board."));

        if (targetMember.getRole() == BoardRole.OWNER) {
            throw new ForbiddenException("Cannot demote or change the role of the board OWNER.");
        }

        if (newRole == BoardRole.OWNER && requesterMember.getRole() != BoardRole.OWNER) {
            log.warn("Privilege escalation attempt: User {} tried to promote user {} to OWNER.", requesterId, targetUserId);
            throw new ForbiddenException("Only the current OWNER can promote another user to OWNER.");
        }

        targetMember.setRole(newRole);
        boardMemberRepository.save(targetMember);

        log.info("Role successfully updated: User {} is now {} on board {}", targetUserId, newRole, boardId);
    }
}




