package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.exception.ForbiddenException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.Board;
import com.unisinos.taskmanager.model.BoardMember;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.model.enums.BoardRole;
import com.unisinos.taskmanager.repository.BoardMemberRepository;
import com.unisinos.taskmanager.repository.BoardRepository;
import com.unisinos.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    /**
     * Adds a user (found by email) to the board as a MEMBER.
     * Enforces RBAC: requester must be OWNER or ADMIN of the board.
     */
    public void addMember(UUID boardId, String emailToAdd, UUID requesterId) {
        requireOwnerOrAdmin(boardId, requesterId);

        User userToAdd = userRepository.findByEmailAndDeletedFalse(emailToAdd)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, userToAdd.getId())) {
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
    }

    /**
     * Removes a member from a board. Enforces RBAC: requester must be OWNER or ADMIN.
     */
    public void removeMember(UUID boardId, UUID userIdToRemove, UUID requesterId) {
        requireOwnerOrAdmin(boardId, requesterId);

        BoardMember target = boardMemberRepository.findByBoardIdAndUserId(boardId, userIdToRemove)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        boardMemberRepository.delete(target);
    }

    private void requireOwnerOrAdmin(UUID boardId, UUID requesterId) {
        BoardMember requesterMember = boardMemberRepository.findByBoardIdAndUserId(boardId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        BoardRole role = requesterMember.getRole();
        if (role != BoardRole.OWNER && role != BoardRole.ADMIN) {
            throw new ForbiddenException("Access denied");
        }
    }
}




